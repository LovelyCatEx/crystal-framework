# Quick Start

## Prerequisites

Before you begin, make sure you have the following tools installed:

- **Git** — Version control
- **Docker Desktop** — Development environment (database, cache, etc.)
- **JDK 17+** — Backend runtime
- **Node.js 20+** — Frontend runtime
- **pnpm** — Frontend package manager

### DevContainer (Recommended)

DevContainer is a Docker container-based development environment solution that provides a **consistent, isolated, and reproducible** development environment.

1. The DevContainer configuration is in the `.devcontainer` folder of the project root directory.
2. Copy the `.env.example` inside and rename it to `.env`.
3. Return to the project root directory.
4. Copy the `.env.example` in the project root directory and rename it to `.env`.
5. Change `POSTGRES_HOST` to `postgres`, `REDIS_HOST` to `redis`, `SNAILJOB_SERVER_HOST` to `snailjob` in the `.env` file.
6. Start the backend project — the database will be automatically initialized.

### Docker

If you prefer not to use DevContainer:

```shell
cd .devcontainer
cp .env.example .env
docker compose up --scale dev-server=0 -d
cd ..
cp .env.example .env
```

## Start Backend

### 1. Clone the Repository

```shell
git clone -b develop https://github.com/LovelyCatEx/crystal-framework.git
cd crystal-framework
git checkout -b [branch-name]
```

### 2. Build

```shell
mvn install -DskipTests
```

### 3. Run

```shell
mvn spring-boot:run -pl crystal-starter
```

Wait for the console to output `Started SpringbootTemplateApplication` to confirm the backend is running.

## Start Frontend

### 1. Install Dependencies

```shell
cd web
pnpm install
```

### 2. Start Dev Server

```shell
pnpm dev
```

### 3. Open in Browser

Navigate to the frontend dev server address. Log in with the default admin account (auto-initialized on first startup) to access the admin dashboard.

## Verification

1. Backend health check: `curl http://localhost:8080/api/v1/actuator/health`
2. Frontend: visit the frontend dev server address

## Submit a PR

1. Develop on your own branch — create a separate branch for each feature or fix.
2. When ready, submit a Pull Request merging from your branch to the `develop` branch.

::: warning
PR titles should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification.
:::
