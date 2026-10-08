# Delivo 公网作品演示部署

本指南适用于「用户页公开，管理端另加访问密码」的作品演示。公网入口使用 Caddy 自动申请和续期 HTTPS 证书；MySQL、Redis、Java 后端和两个 Nginx 前端只在 Docker 网络内通信。真实支付接口在公网入口关闭，用户页使用模拟支付。

## 0. 先准备什么

部署文件目前在本地工作区；先将代码和这些配置提交、推送到自己的 GitHub 仓库，确认 Actions 的 CI 通过。只提交 `.env.public.example`，不要提交 `.env` 或 `.env.public`。

1. 一台可通过 SSH 登录的 Ubuntu 云服务器，且有公网 IPv4 地址。作品演示建议从 Ubuntu 24.04 LTS、x86_64、2 vCPU、4 GB 内存、40 GB SSD 起步。这是初始配置建议，实际余量需上线后观察。服务器上安装 Docker Engine 和 Compose 插件，确认 `docker compose version` 可运行。安装步骤以 [Docker 官方 Ubuntu 文档](https://docs.docker.com/engine/install/ubuntu/) 为准。下文假设登录用户能运行 `docker`；如需 `sudo`，在 Docker 命令前加 `sudo`。
2. 一个域名，规划两个子域名，例如 `demo.example.com` 和 `admin.example.com`。分别为两者添加 A 记录，指向服务器的公网 IPv4。等 DNS 生效后再启动 Caddy。
3. 在云平台的安全组中开放 TCP 80、TCP 443，SSH 的 TCP 22 只允许自己的 IP；若启用 QUIC，可另开 UDP 443。Caddy 获取公网证书需要域名正确指向服务器、80/443 可从公网访问，见 [Caddy HTTPS 文档](https://caddyserver.com/docs/quick-starts/https)。
4. 确认服务器磁盘有空间存放 Docker 镜像、数据库和备份。此项目会在服务器上编译 Java 与两个 Vue 应用。

### 选服务器与准备 SSH

VPS 就是一台租来的、保持联网的电脑。我们把 Docker 和 Delivo 放在它上面，本地电脑关机后网站仍能运行。SSH 是从本地终端登录这台远程电脑的方式。

国内和欧洲都有人访问时，低预算可以先用欧洲机房试运行。国内连接质量需要上线后实测，机房位置本身不能保证访问速度。演示初期选月付，查看订单里的含税总价、续费价格和续费周期。

2026-10-06 核对的一个低价候选是 [OVHcloud VPS-1](https://www.ovhcloud.com/fr/vps/)：2 vCPU、4 GB 内存、40 GB NVMe。在配置页选择 `Aucun engagement`（无长期合约）、英国 Erith、Ubuntu 24.04、不加付费选项，报价为 €4.49/月未税；按法国 20% TVA 计算约 €5.39/月含税。价格、库存和税额以自己的最终订单为准，域名另算。网页宣传的 €3.81/月未税起价对应 12 个月预付选项。

Windows 上可使用系统自带的 OpenSSH。没有专用密钥时，在 PowerShell 中生成（若同名文件已存在，不要覆盖）：

```powershell
ssh-keygen -t ed25519 -f "$env:USERPROFILE\.ssh\id_ed25519_delivo" -C "delivo-server"
```

生成两个文件：`.pub` 是公钥，可以放到云平台的 SSH 公钥设置里；另一个是私钥，保存在本机。若创建服务器时提供 SSH 公钥选项，复制 `.pub` 文件的完整一行内容。若只提供首次登录密码，先按云平台指引登录，再配置公钥。

服务器就绪后，记下公网 IPv4、登录用户名和 SSH 端口。确认服务器已安装对应公钥后，在本地 PowerShell 执行下面的命令；将用户名、IP 和端口替换成云平台提供的值：

```powershell
ssh -i "$env:USERPROFILE\.ssh\id_ed25519_delivo" -p 22 登录用户名@服务器公网IP
```

首次连接出现主机指纹提示时，先与云平台控制台中服务器的主机指纹核对。成功登录后，下面的 `bash` 命令都在服务器的 SSH 终端中执行。

## 1. 把代码放到服务器

在服务器的 SSH 终端中执行。把 GitHub 地址换成自己的仓库地址：

```bash
git clone https://github.com/ToxicCalvin/delivo.git
cd delivo
cp .env.public.example .env.public
chmod 600 .env.public
```

本地开发用的 `.env` 不要复制到服务器。`.env.public` 已加入 `.gitignore`。

## 2. 填写公网环境变量

用 `nano .env.public` 编辑文件：

- `USER_DOMAIN` 和 `ADMIN_DOMAIN` 写两个真实子域名，不能相同。
- 分别为 `MYSQL_ROOT_PASSWORD`、`MYSQL_APP_PASSWORD`、`JWT_ADMIN_SECRET`、`JWT_USER_SECRET`、`ADMIN_APP_PASSWORD` 生成不同的随机值。可运行 `openssl rand -hex 32`，每次复制一条新结果。`ADMIN_APP_PASSWORD` 是管理后台应用内 `admin` 账号的新密码，只在空数据库首次初始化时写入。
- `OPENAI_API_KEY`、`DASHSCOPE_API_KEY` 填可用的 API Key。为公开演示设置供应商侧的额度或费用上限。
- `ADMIN_BASIC_AUTH_USER` 是浏览器弹窗的用户名。再选一个独立的长密码作为浏览器弹窗密码。运行 `docker run --rm -it caddy:2 caddy hash-password`，在提示时输入这个密码；把输出的哈希放入 `ADMIN_BASIC_AUTH_HASH` 的单引号内。记住原密码，哈希无法还原。Caddy 要求这里填写哈希，见 [basic_auth 文档](https://caddyserver.com/docs/caddyfile/directives/basic_auth)。

管理端需要两次登录：先输入浏览器弹窗的账号密码，再输入应用账号 `admin` 和 `ADMIN_APP_PASSWORD`。不要使用 README 里的本地默认密码登录公网环境。

检查文件是否填全时，运行下面的命令。它不打印变量值：

```bash
docker compose --env-file .env.public -f compose.public.yml config --quiet
```

## 3. 启动

确认两个域名的 A 记录已指向服务器，且 80/443 可访问，然后执行：

```bash
docker compose --parallel 1 --env-file .env.public -f compose.public.yml build
docker compose --env-file .env.public -f compose.public.yml up -d
docker compose --env-file .env.public -f compose.public.yml ps
docker compose --env-file .env.public -f compose.public.yml logs --tail=100 caddy delivo-backend
```

首次构建与数据库初始化可能需要几分钟。`--parallel 1` 让三个应用依次构建，降低小内存服务器同时编译的压力；它不限制单个应用构建内部的内存用量，见 [Compose 并行控制文档](https://docs.docker.com/reference/cli/docker/compose/#configuring-parallelism)。MySQL 首次建立数据卷时会导入菜单数据并把种子管理员密码改成 `ADMIN_APP_PASSWORD`。若数据卷已存在，更改这个环境变量**不会**自动改库中的密码。

打开 `https://用户域名` 和 `https://管理域名`。如果出现证书错误，先检查 DNS、云安全组和 Caddy 日志；不要改成 HTTP 绕过。

## 4. 验收核心流程

1. 用户域名：菜单和店铺状态可加载；注册或登录一个**演示账号**；购物车、模拟下单与模拟支付能完成；AI 助手能回复。不要输入真实地址、电话或付款资料。
2. 管理域名：未输入浏览器弹窗密码时应得到 401；输入正确后，使用 `admin` 和 `ADMIN_APP_PASSWORD` 登录；查看订单和菜单。
3. 用户域名的 `/api/admin/employee/login` 与 `/ws/test` 应为 404；`/api/user/shop/status` 应正常响应。管理域名全站都受额外密码保护。
4. GitHub 仓库的 Actions 页面应显示 CI 通过：Java 单元测试、两个前端构建、Compose 配置校验。CI 暂时只验证代码，不自动上线；服务器和 SSH 凭据就绪后再接自动部署。

当前 WebSocket 服务没有连接鉴权，公网用户页因此关闭实时通知连接；订单状态仍可在页面中刷新查看。管理域名下的 WebSocket 由额外密码保护。

## 5. 日志、更新、备份

查看服务日志：

```bash
docker compose --env-file .env.public -f compose.public.yml logs -f --tail=100 delivo-backend
```

Compose 对容器日志设置了大小与文件数上限。不要把 `.env.public`、完整日志或数据库备份公开上传；日志可能包含用户输入。

更新前先在 GitHub Actions 确认 CI 通过，再在服务器执行：

```bash
git pull --ff-only
docker compose --parallel 1 --env-file .env.public -f compose.public.yml build
docker compose --env-file .env.public -f compose.public.yml up -d
```

数据库使用命名数据卷；`docker compose down` 不删除数据卷，`down -v` 会删除数据，应避免执行。定期做数据库备份，例如：

```bash
umask 077
docker compose --env-file .env.public -f compose.public.yml exec -T mysql sh -c 'exec mysqldump --single-transaction -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' > "delivo-$(date +%F).sql"
```

备份文件应保存在服务器之外的私有位置，并定期检查可恢复性。公网演示仍使用旧式 MD5 账号密码实现，适合展示项目，不适合承载真实客户信息或正式交易。
