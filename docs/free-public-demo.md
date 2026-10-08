# Delivo 免费临时公网演示

改造前后、文件内容及配合方式见[本次技术复盘](free-public-demo-retrospective.md)。本篇用于日常操作。

本方案使用本机 Docker 运行 Delivo，再通过两个 Cloudflare Quick Tunnel 提供临时 HTTPS 链接。无需购买服务器、域名或注册 Cloudflare 账号。电脑、Docker 和隧道必须保持运行；AI 接口仍按供应商规则计费。

## 访问链路

用户浏览器 → Cloudflare HTTPS → user-tunnel → Caddy 8080 → 用户前端／用户 API

管理员浏览器 → Cloudflare HTTPS → admin-tunnel → Caddy 8081（额外密码）→ 管理前端／管理 API／管理 WebSocket

前端 Nginx 转发 API 到 Java 后端；后端使用同一 Docker 网络内的 MySQL、Redis。公网 HTTPS 证书由 Cloudflare 管理，Caddy 在本机容器网络内使用 HTTP。

## 第一次启动（Windows PowerShell）

1. 打开 Docker Desktop，等待 Linux 引擎就绪。
2. 项目根目录 `.env` 中需已有 `OPENAI_API_KEY`、`DASHSCOPE_API_KEY`。
3. 在项目根目录运行：

```powershell
./deploy/start-demo.ps1
```

脚本会生成独立的 `.env.demo`，复制两个 AI Key，生成数据库、JWT 和管理端密码，检查配置，构建和启动应用。入口检查通过后才启动两个隧道。首次启动会将演示店铺设为营业中，后续启动保留已有状态。

构建好的版本再次启动可跳过构建：

```powershell
./deploy/start-demo.ps1 -SkipBuild
```

脚本支持 Windows PowerShell 5.1 和 PowerShell 7。如果 Windows 提示脚本执行策略限制，可对本次命令使用：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ./deploy/start-demo.ps1
```

仅本次进程使用该设置，不修改系统执行策略。

## 地址和密码

- 当前公网地址：`.demo/urls.json`。
- 管理端两层密码：`.demo/credentials.txt`。先输入浏览器入口账号，再使用 Delivo 的 `admin` 账号登录。
- 本机用户页：`http://127.0.0.1:18081`。
- 本机管理页：`http://127.0.0.1:18082`，同样需要入口密码。
- `.env.demo`、`.demo/` 均已加入 Git 忽略；只分享需要分享的链接及演示访问密码，不分享 AI Key、数据库密码或 JWT 密钥。

每个隧道重新创建时会获得新域名。修改应用通常不需要重启隧道；重新启动后查看地址文件，避免继续使用旧链接。

## 数据与公开范围

`compose.demo.yml` 使用独立项目名 `delivo-demo`，MySQL、Redis、AI 检索缓存分别保存在独立的命名卷中。它不导入你本地已有的用户和订单。种子 SQL 仅在空数据库第一次启动时执行；启动脚本不会重置已有演示数据。

MySQL、Redis、Java 后端、前端容器都不直接映射到宿主机端口；只有 Caddy 的两个端口绑定 `127.0.0.1`。两个隧道只能进入对应的 Caddy 入口。

用户入口仅允许 `/api/user/*`，管理 API 和公开 WebSocket 被阻止，真实支付接口被阻止，模拟支付保留。用户端使用 `VITE_PUBLIC_DEMO=true` 构建，关闭当前未鉴权的用户 WebSocket。管理端网页、API 和 WebSocket 全部要求入口密码，应用自身登录校验继续生效。

初始化脚本会替换演示数据库中默认 `admin` 密码。已有数据库初始化后，修改 `.env.demo` 的 `ADMIN_APP_PASSWORD` 不会自动改变数据库中的密码，应通过应用或专门的数据库更新操作修改，避免删卷重建。

这是模拟订单的作品演示。不要输入真实支付资料、个人收货信息；现有应用的演示限制仍然适用，未配置的 OSS 图片上传不由隧道方案补齐。

## 验证

```powershell
./deploy/verify-demo.ps1
./deploy/verify-demo.ps1 -Public
```

验证会创建一个新的测试用户、虚拟地址和一笔模拟订单，检查菜单、登录、购物车、模拟支付、管理端订单和 WebSocket 通知，并发送一条 AI 菜品推荐请求。它不会调用真实支付。每次会留下这笔明确标记的演示数据；结果保存到 `.demo/last-check.json`。

不调用 AI 时使用 `-SkipAi`。公网验证包含用户接口范围、支付限制，以及管理端页面、API、WebSocket 的入口密码检查。

还需要人工用手机关闭 Wi-Fi 后访问用户链接，以及让中国、欧洲的访问者分别验证。开发电脑自己能打开链接不等于所有地区均能访问。

## 日志

```powershell
docker compose --env-file .env.demo -f compose.demo.yml --profile tunnel ps
docker compose --env-file .env.demo -f compose.demo.yml logs --tail 80 delivo-backend
docker compose --env-file .env.demo -f compose.demo.yml logs --tail 80 caddy
docker compose --env-file .env.demo -f compose.demo.yml logs --tail 80 user-tunnel admin-tunnel
```

所有容器日志按每份 10 MB、最多 5 份轮转。Caddy 记录入口访问日志，后端使用已有 `prod` 配置减少业务日志。不要执行或分享不带 `--quiet` 的 Compose 展开配置，其内容可能包含密钥。

## 停止演示

```powershell
./deploy/stop-demo.ps1
```

此命令停止并移除演示容器和隧道，保留数据卷。公网链接随后失效。不要加 `down -v`，该选项会删除演示数据。

若只想立即切断公网访问、保留应用运行：

```powershell
docker compose --env-file .env.demo -f compose.demo.yml --profile tunnel stop user-tunnel admin-tunnel
```

## GitHub Actions

CI 运行核心后端测试、两个前端构建，并检查两套公网 Compose 和 Caddy 配置。检查内容、运行方法及日志入口见 [CI 使用说明](continuous-integration.md)。运行电脑上的临时隧道由启动脚本负责。GitHub Actions 云端运行器无法直接操作这台本机；这次无需配置自动公网发布。

## 免费服务限制

Quick Tunnel 没有可用性保证，每个隧道最多 200 个同时进行的请求，不支持 SSE。当前 Delivo AI 聊天使用普通 JSON 请求，未使用 SSE。域名每次创建会变化，电脑休眠、断网或关闭 Docker 都会中断访问。

如果隧道迟迟未连通，先检查日志、网络代理和防火墙对 Cloudflare 隧道连接的影响，修复后重启对应隧道。该配置使用 HTTP/2 的 TCP 连接，仍需要允许隧道使用的出站端口。

官方说明：https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/
