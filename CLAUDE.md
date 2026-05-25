# CLAUDE.md

本文档为 Claude Code（claude.ai/code）在此仓库中工作时提供指导。

## 构建与开发命令

### 前端（web/）
- `cd web && pnpm dev` — 启动 Vite 开发服务器
- `cd web && pnpm build` — TypeScript 检查 + Vite 构建（`tsc -b && vite build`）
- `cd web && pnpm lint` — ESLint 检查
- `cd web && npx tsc --noEmit` — 仅 TypeScript 检查（不构建）

### 后端（根目录）
- `./mvnw clean install -DskipTests` — 构建所有模块
- `./mvnw clean install -pl crystal-starter -am -DskipTests` — 构建 starter 及其依赖
- 后端依赖：Java 17+、PostgreSQL、Redis

## 项目架构

CrystalFramework 是一个 Spring Boot 4 + React 19 企业级框架，支持多租户、RBAC、OAuth2 和基于插件的可扩展性。

### 后端（Maven 多模块，Kotlin 2.3 + Java 17）

| 模块                     | 用途                                          |
|------------------------|---------------------------------------------|
| `crystal-starter`      | Spring Boot 主应用 — 聚合所有模块、认证、RBAC、用户、租户、系统设置 |
| `crystal-shared`       | 共享基础设施：基础实体、仓库、常量、响应封装、配置                   |
| `crystal-shared-types` | 跨模块类型定义                                     |
| `crystal-audit`        | 基于 AOP 的审计日志                                |
| `crystal-schedule`     | 任务调度（Spring Cron + SnailJob）                |
| `crystal-resource`     | 文件存储（本地、阿里云 OSS、腾讯 COS）                     |
| `crystal-encrypt`      | RSA+AES API 加密                              |
| `crystal-mail`         | 邮件模板与发送                                     |
| `crystal-monitor`      | 系统监控                                        |
| `crystal-sdk`          | 模块化 SDK，用于注册数据库表、RBAC 配置等                   |

技术栈：Spring Boot 4.0、Spring WebFlux、R2DBC、PostgreSQL、Redis、Flyway、Spring Security、JJWT、SnailJob。

### 前端（web/，React 19 + TypeScript）

| 目录                  | 用途                                                                                     |
|---------------------|----------------------------------------------------------------------------------------|
| `src/api/`          | Axios API 客户端 — `request.ts` 封装、各实体对应的 `.api.ts` 文件、`BaseManagerController.ts` 通用 CRUD |
| `src/assets/`       | 静态资源（图片、字体等）                                                                           |
| `src/components/`   | 可复用 UI 组件（`ContextMenu`、`Watermark` 等）                                                 |
| `src/compositions/` | 自定义 React Hooks                                                                        |
| `src/config/`       | 应用配置                                                                                   |
| `src/contexts/`     | React Context 提供者（`SystemIntegratedContext`、`useLoggedUser`、`useUserTenants`）          |
| `src/global/`       | 全局状态（用户、租户信息）                                                                          |
| `src/i18n/`         | i18next + react-i18next — 完整的 en-US/zh-CN 覆盖                                           |
| `src/pages/`        | 页面组件（`ManagerContainerPage`、登录等）                                                       |
| `src/plugin/`       | 插件系统 — 插件可扩展菜单和路由                                                                      |
| `src/router/`       | 路由定义（`react-router-dom` 7，`/manager/*` 为需要认证的管理后台）                                     |
| `src/types/`        | TypeScript 类型定义                                                                        |
| `src/utils/`        | 工具函数                                                                                   |

技术栈：React 19、Ant Design 6、TailwindCSS 3、Vite 7、pnpm、SWR（无全局状态管理）、@dnd-kit。

路径别名：`@/` → `src/`（定义在 `vite.config.ts` resolve.alias 和 `tsconfig.app.json` paths 中）。

### 关键前端组件层级

```
main.tsx → BrowserRouter
  ProtectedApp        — 启动时生成 RSA 密钥对
    App               — antd ConfigProvider、SystemIntegratedProvider
      ManagerContainerPage  — 布局：Header + Sider + Content
        ManagerPageTabs     — 可拖拽/关闭的标签栏（持久化到 localStorage）
        Watermark
        Routes              — 每个菜单项渲染一个页面组件
```

## 编码规范

**!!!Attention: 在进行任何文件创建、代码编写/修改之前，应当阅读下面的内容，严格遵守规则，若用户意图与以下任意一条规则冲突，必须告知用户并得到二次确认才可以继续，否则视为违规操作。!!!**
**!!!Attention: 在遇到该文档中未能详细描述或缺失的问题，请前往项目根目录 `.agent` 目录中寻找相关说明，若无则以最小的代价且符合所有下述规则的前提下进行修改。!!!**

### 后端编码规范

后端采用模块化开发，现有结构请见上方的 Project Architecture 部分。

一般的模块内部结构以下列包为基础：

+ annotations: 存放自定义注解，用于标记特定行为、元数据或切点。
+ aspect: 实现AOP切面逻辑，将横切关注点（如日志、鉴权、限流）与业务代码解耦。
+ config: 负责模块的配置类，定义Bean、参数绑定、初始化设置等。
+ controller: 接收并解析外部请求，调用服务层处理后返回响应（通常为API入口）。
+ entity: 定义与数据库表映射的实体类，通常配合ORM框架使用。
+ repository: 数据访问层接口，封装对数据库的CRUD操作（如JPA、MyBatis-Plus Mapper）。
+ service: 业务逻辑接口层，定义核心业务操作契约。
+ service/impl: 业务逻辑的具体实现类，实现service接口并处理事务、调用仓储等。
+ types: 定义领域自定义类型、值对象或枚举，增强类型安全与业务语义。
+ constants: 存放模块内通用的常量定义（如状态码、错误信息、配置键名）。
+ event: 定义领域事件、事件监听器及发布逻辑，支持模块内或跨模块异步解耦。
+ utils: 提供与业务无关的通用工具方法（如日期转换、加密、字符串处理）。

模块的基础包名一般以 `com.lovelycatv.crystalframework` 开头，且子包名为本模块的名称。

例如 mail 模块的包名为 `com.lovelycatv.crystalframework.mail`。

#### Controller

一个 Controller 必须包含下面的所有注解：

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/dashboard")
class DashboardController
```

普通 Controller 只需要放入 `controller` 包即可，对于 Manager 页面用到的 Controller 必须放入 `controller/manager` 中，并且类的命名与相关 DTO 文件必须以 `Manager` 开头。

`controller` 包中一般情况只允许有普通 Controller 文件、`dto`、`vo`、`manager` 包，而 `manager` 包的结构同理。

**标准化 ManagerController**

对于标准化 Controller 请见 `StandardManagerController` 的相关子类，此类型的 Controller 必须继承该类或该类的其他变种（按需）。

示例请参考 `ManagerOAuthAccountController` 类。

**非标准化 Controller**

默认情况下，所有接口都要求访问时携带合法 Token，但允许使用 `@Unauthorized` 注解表示无需任何授权即可访问的接口。

本项目使用 SpringSecurity 进行权限校验，因此你可以使用任意该框架的权限校验注解，例如 `@PreAuthorize` 等。

#### 实体类

关于实体类请见项目根目录下的 `.agent` 文件夹中的 `AddBaseEntity.md`。

**后端 BaseEntity**（`crystal-shared` 模块）包含 4 个字段，序列化到前端时 `Long` 变为 String：

| 字段             | Kotlin 类型 | 前端接收类型   | 说明                          |
|----------------|-----------|----------|-----------------------------|
| `id`           | `Long`    | `string` | 雪花算法主键，不可用自增                |
| `createdTime`  | `Long`    | `string` | 创建时间戳（毫秒）                   |
| `modifiedTime` | `Long`    | `string` | 修改时间戳（毫秒），`onUpdate()` 自动维护 |
| `deletedTime`  | `Long?`   | —        | 软删除时间戳，SQL 拦截器自动过滤          |

核心方法：`newEntity()` 标记为新记录、`onUpdate()` 刷新 `modifiedTime`、`softDelete()` / `restore()` / `isDeleted()` 管理软删除。

**前端 BaseEntity**（`types/BaseEntity.ts`）：

```typescript
export interface BaseEntity {
    id: string;          // 后端 Long 经 ToStringSerializer 序列化为 String
    createdTime: string;
    modifiedTime: string;
}
```

#### Repository

关于 Repository 接口必须继承 `BaseRepository`，特殊情况（非 BaseEntity 子类的实体类）除外。

#### Service

本项目使用 `service` / `service/impl` 的包结构。

对于 ManagerController 对应的 Service，必须放入 `service/manager` 中，同样遵守 `service/manager/impl` 结构，且该类必须继承 CachedBaseManagerService，有实体从属关系的请见 `EntityRelationshipCheckService` 的相关用法。

其他实体类对应的 Service 必须继承 `CachedBaseService` 以便使用实体对象缓存。

对于实体的删改操作，请见 `CachedBaseService` 中的 `withUpdateEntityContext` / `withDeleteEntityContext` 等方法，**禁止直接删改实体对象**，必须用 `withXXXContext` 包裹相关对象的操作。

#### Aspect

所有 Aspect 必须有 `@Order` 注解，且优先级必须使用 `GlobalConstants.AspectPriority` 定义。

#### Filter

所有 Filter 必须有 `@Order` 注解，且优先级必须使用 `GlobalConstants.FilterPriority` 定义。

### 前端编码规范

以下规则均以 `web/` 作为工作目录，不再重复。

#### 组件

组件必须放入 `components` 文件夹，一般情况下组件是单个 tsx 文件并且直接放入该文件夹即可。

特殊情况，例如 `columns` / `selector` / `card/pop` 等同类型有多个变种的情况，必须建立文件夹一起存放。

**写组件之前先检查是否有类似的文件夹存放你要写的组件，若有必须放入对应位置，若没有请遵守上述规则。**

对于组件中用到的类型/枚举，必须在 tsx 文件内的顶部（import 下方）使用 export 导出，对于组件内部的类型/枚举等不需要 export 导出。

#### Context

所有需要创建并提供上下文的组件，必须放入 `context` 文件夹而非 `compositions` 中。

#### Api

所有 Api 文件必须放入 `api` 中的对应文件夹，必须按后端对应的模块/包进行分类，但 `request` / `system-request` 等特殊类型的 api 文件无需分类。

所有相关的 DTO 文件必须写在相应 api 文件的头部/尾部，对于 VO 等返回类型必须放在 `types` 文件夹中，目录结构严格对应 `api` 文件夹。

对于后端继承 BaseEntity 的实体类，前端对应也必须继承该类。

对于后端的标准化的控制器（StandardManagerController）必须继承前端的 BaseManagerController，写法参照其他相关文件，若无特殊情况，均以 `export const` 的方式导出。

**BaseManagerController**（`api/BaseManagerController.ts`）有 5 个泛型参数和 5 个方法：

```typescript
class BaseManagerController<ENTITY, C, R = BaseManagerReadDTO, U = BaseManagerUpdateDTO, D = BaseManagerDeleteDTO> {}
```

| 泛型参数     | 说明                                                                                       |
|----------|------------------------------------------------------------------------------------------|
| `ENTITY` | 实体类型（对应后端的实体类）                                                                           |
| `C`      | Create DTO                                                                               |
| `R`      | Read/Query DTO（默认 BaseManagerReadDTO，含 page、pageSize、id、searchKeyword、startTime、endTime） |
| `U`      | Update DTO（默认 BaseManagerUpdateDTO，含 id）                                                 |
| `D`      | Delete DTO（默认 BaseManagerDeleteDTO，含 ids: string[]）                                      |

| 方法              | 请求方式     | URL 模式                 | 说明                            |
|-----------------|----------|------------------------|-------------------------------|
| `create(dto)`   | POST     | `/api{baseUrl}/create` | 新增                            |
| `query(dto)`    | GET      | `/api{baseUrl}/query`  | 分页查询，返回 PaginatedResponseData |
| `update(dto)`   | POST     | `/api{baseUrl}/update` | 更新                            |
| `delete(dto)`   | POST     | `/api{baseUrl}/delete` | 删除                            |
| `list(params?)` | GET      | `/api{baseUrl}/list`   | 全量列表                          |
| `getById(id)`   | 复用 query | —                      | 按 ID 查单条                      |

**请求路径拼接规则**：`/api` + `baseUrl`（即构造时传入的路径，如 `/manager/mail-send-logs`）+ 方法路径。开发环境下 Vite 代理 `/api` → 后端 dev server。

调用 api 时，先判断该 api 是否有缓存的价值，本项目提供预设的 SWR-Composition 可用，位于 `compositions` 文件夹。内置多种方式，请按需使用。

#### Compositions

所有自定义钩子函数必须放入 `compositions` 文件夹，文件名必须以 `use-` 开头，例如 `use-logged-user` 且内部函数名也应该同名且符合命名规范。

#### Config

配置文件位于 `config` 文件夹。

如需添加环境变量，必须在 `config/env.ts` 同步添加，并在需要读取的地方使用 `currentEnvironment` 获取。

#### I18N 国际化

国际化文件在 `i18n` 文件夹中，接下来该部分均以 `web/i18n` 作为工作目录。

语言文件在 `locales` 文件夹中，文件命名必须是语言标准全名，例如 `en-US` / `zh-CN` 等。

语言文件导出的对象必须使用 `i18n-rules.ts` 中的类型，严格遵守类型规范。

如遇枚举类型需要翻译，必须写入 `enum-helpers.ts` 中，且 i18n 文件也必须遵守该规则。

关于系统设置的国际化，请看 `system-settings.tsx` 文件。

#### 页面

所有页面必须放入 `pages` 文件夹，且按照类型区分子目录。

对于标准化的 Manager 页面请使用 `ManagerPageContainer` 组件，对于非标准化的页面可以按需使用组件，但一般情况下至少要有 `ActionBarComponent` 标题组件。

**ManagerPageContainer 核心 Props**（`components/ManagerPageContainer.tsx`）：

| Prop                    | 类型                                                   | 说明                                               |
|-------------------------|------------------------------------------------------|--------------------------------------------------|
| `entityName`            | `string`                                             | 实体名称（用于弹窗标题等，通常传 `t('entityNames.xxx')`）         |
| `columns`               | `EntityTableColumns<ENTITY>`                         | 表格列定义                                            |
| `query`                 | `(dto: R) => Promise<PaginatedResponseData<ENTITY>>` | 分页查询函数                                           |
| `delete`                | `(dto) => Promise<unknown>`                          | 删除函数                                             |
| `update`                | `(dto) => Promise<unknown>`                          | 更新函数                                             |
| `create`                | `(dto) => Promise<unknown>`                          | 新增函数                                             |
| `readonlyMode`          | `boolean`                                            | 只读模式（隐藏增删改按钮）                                    |
| `showRowActions`        | `boolean`                                            | 是否显示行操作按钮（编辑/删除），默认 `true`，只读模式自动隐藏              |
| `showActionBar`         | `boolean`                                            | 是否显示顶栏，默认 `true`                                 |
| `tableActions`          | `array`                                              | 表格筛选条件（每个元素含 label、children、queryParamsProvider） |
| `editModalFormChildren` | `ReactNode \| ((ENTITY) => ReactNode)`               | 新增/编辑弹窗的表单项                                      |
| `showTimeRangeFilter`   | `boolean`                                            | 是否显示时间范围筛选器，默认 `true`                            |

注意 `tableActions` 中的 `queryParamsProvider` 会在查询时调用，返回值会和 `query` 的 DTO 合并。筛选条件变化时需手动触发 `pageRef.current.refreshData({resetPage: true})`（见现有页面示例）。

#### 插件

前端插件相关代码在 `plugin` 文件夹中，若**非必要禁止修改**本文件夹中的任意文件。

#### 路由

Manager 页面侧边栏的路由文件位于 `router` 文件夹，并且只提供以下三种类型的目录：

1. Public: 登录后无需权限即可访问的页面。
2. Admin: 登录后需要相关权限才可访问的页面。
3. Tenant: 以租户的身份登录后才可访问的页面。

所有路由将会通过 `computeAccessibleMenus` 函数计算得出。

若需要添加其他非 Manager 页面的路由，请见 `App.tsx` 中的 `<Routes>` 标签。

#### 工具函数

所有工具函数必须放入 `utils` 文件夹。
