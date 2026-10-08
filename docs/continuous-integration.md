# Delivo GitHub Actions 使用说明

[![CI](https://github.com/ToxicCalvin/delivo/actions/workflows/ci.yml/badge.svg)](https://github.com/ToxicCalvin/delivo/actions/workflows/ci.yml)

CI 是“提交后自动检查”。工作流保存在 [ci.yml](../.github/workflows/ci.yml)，在 GitHub 提供的临时 Linux 环境里执行，不依赖开发电脑是否开机。

## 每次检查什么？

| GitHub 中的任务 | 做什么 | 能发现什么 |
| --- | --- | --- |
| `backend` | 使用 Java 17，执行 `mvn -B -f delivo/pom.xml test`，编译后端并运行已有单元测试 | 编译错误、已覆盖的业务逻辑回归 |
| `web (delivo-user-web)` | 使用 Node.js 20，执行 `npm ci` 和 `npm run build` | 用户端依赖安装、类型检查和打包错误 |
| `web (delivo-admin-web)` | 使用 Node.js 20，执行 `npm ci` 和 `npm run build` | 管理端依赖安装和打包错误 |
| `deployment-config` | 校验云服务器和免费临时演示两套 Compose、Caddy 配置 | 必填变量、Compose 结构、Caddy 配置语法等问题 |

`npm ci` 按已提交的 `package-lock.json` 安装依赖。修改依赖时，需同时提交对应的锁文件。

配置检查使用 `ci-placeholder` 等测试值，不需要 GitHub Secrets，也不需要真实 AI Key、数据库或管理员密码。工作流只有仓库内容读取权限，各任务有超时限制。

## 怎么触发和看结果？

1. 将提交推送到 GitHub，或者创建／更新 Pull Request，CI 会自动启动。
2. 打开仓库的 [Actions 页面](https://github.com/ToxicCalvin/delivo/actions/workflows/ci.yml)，选择对应提交的运行记录。
3. 四个任务都显示绿色勾号，才表示这一版本的 CI 检查通过。
4. 如果出现红叉，点击失败任务，再展开失败步骤查看日志；修改代码并推送新的提交后会重新检查。

也可以在该页面选择 **Run workflow**，指定分支手动运行。对于临时网络失败，可以从原记录选择 **Re-run failed jobs**。

判断一个版本是否通过时，核对运行记录的分支和提交编号，避免把旧版本的成功结果当作当前版本的结果。

## 通过后能说明什么？

说明该提交的已有后端单元测试、两个前端构建和部署配置校验在 GitHub 环境中通过，之后提交也会自动接受这些检查。

CI 不调用真实 AI 服务，不启动完整数据库业务环境，也不执行本机的公网验证脚本。公网访问、模拟下单、管理端通知和 AI 推荐仍需按照 [免费演示指南](free-public-demo.md) 运行验证。

目前不构建或发布 Docker 镜像，也不自动更新本机正在演示的版本。应用修改后，需要在电脑上重新运行 `./deploy/start-demo.ps1` 构建并更新演示；`-SkipBuild` 只适用于已有镜像可以继续使用的情况。
