# 源码贡献快速开始

## 环境准备

开始之前请确保已安装以下工具：

- **Git** — 版本控制
- **Docker Desktop** — 运行开发环境（数据库、缓存等）
- **JDK 17+** — 后端运行环境
- **Node.js 20+** — 前端运行环境
- **pnpm** — 前端包管理器

### DevContainer（推荐）

DevContainer 是一种基于 Docker 容器的开发环境方案，由微软主导推动，旨在为开发者提供一致、隔离、可复现的开发环境。

1. DevContainer 的配置在项目根目录的 `.devcontainer` 文件夹中，进入此文件夹。
2. 将里面的 `.env.example` 复制一份并改名为 `.env`，若非必要请不要随意修改里面的内容。
3. 完成上述步骤后回到项目根目录。
4. 再将项目根目录中的 `.env.example` 复制一份并改名为 `.env`。
5. 将上一步的 `.env` 文件中 `POSTGRES_HOST` 的值改为 `postgres`、`REDIS_HOST` 的值改成 `redis`、`SNAILJOB_SERVER_HOST` 的值改成 `snailjob`，保存。
6. 此时你可以启动后端项目，数据库将会自动初始化。

### Docker

如果不想使用 DevContainer，也可以直接使用 Docker Compose：

```shell
cd .devcontainer
cp .env.example .env
docker compose up --scale dev-server=0 -d
cd ..
cp .env.example .env
```

## 启动后端

### 1. 克隆仓库

```shell
git clone -b develop https://github.com/LovelyCatEx/crystal-framework.git
cd crystal-framework
git checkout -b [分支名]
```

### 2. 编译

```shell
mvn install -DskipTests
```

### 3. 启动

```shell
mvn spring-boot:run -pl crystal-starter
```

等待控制台输出 `Started SpringbootTemplateApplication` 即表示启动成功。

## 启动前端

### 1. 安装依赖

```shell
cd web
pnpm install
```

### 2. 启动开发服务器

```shell
pnpm dev
```

### 3. 访问页面

打开浏览器访问前端开发服务器地址。使用默认管理员账号登录（首次启动时系统会自动初始化）即可进入管理后台。

## 验证

1. 后端健康检查：`curl http://localhost:8080/api/v1/actuator/health`
2. 前端页面：访问前端开发服务器地址

## 提交 PR

1. 在你创建的分支进行开发，建议一个功能/修改单独开一个分支进行
2. 当所有开发工作完成后，请向本项目发起 Pull Request，从你的分支合并到 develop 分支

::: warning
本项目只允许从你的分支合并到 develop 分支，PR 的标题请**尽量**遵守如下命名规范: [Commit 信息编写规范](https://www.conventionalcommits.org/zh-hans/v1.0.0/)
:::
