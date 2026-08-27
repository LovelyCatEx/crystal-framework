# 更新日志

## 公告

::: danger
1. v1.5.* ~ v1.9.* 版本存在严重的安全漏洞，请勿使用，详见 v1.10.0 更新日志。
2. v1.13.0 开始使用权限矩阵模式代替原有权限，迁移到该版本是破坏性的操作。
:::

## v1.14.3

2026-08-27

### 新功能
+ feat(resource): 使存储提供商类型可通过 SDK 扩展。
+ feat(resource): 使资源文件类型可通过 SDK Registry 扩展。
+ feat(resource): 通过 SDK Registry 暴露资源文件类型扩展能力。
+ feat(resource): 使存储提供商与资源文件类型支持 SDK 扩展。

### Bug 修复
+ fix(resource): 在管理器筛选器和弹窗中显示本地化的资源文件类型标签。

### 其他
+ build(backend): 新增 dependency-check 与 git-commit-id 插件，并丰富启动横幅信息。
+ build(backend): 引入 detekt 静态分析与 enforcer 规则。

---

## v1.14.2

2026-08-20

### 新功能
+ feat(message): 新增站内消息，支持广播（读扩散）与会话（写扩散）两类消息模型。
+ feat(message): 新增 IM 风格广播收件箱与会话历史分页。
+ feat(message): 新增用户与租户客服会话，收件箱按身份分区。
+ feat(message): 新增租户成员会话与统一发起会话弹窗。
+ feat(message): 新增基于身份分区的收件箱标签页与跨组织成员会话。
+ feat(message): 新增按 segment 驱动的发起会话对话框，支持精确用户搜索与系统/租户会话隔离。
+ feat(message): 新增消息功能模块开关。
+ feat(web): 新增头部系统广播通知中心。
+ feat(web): 新增消息中心页面、聚合未读角标与会话分页。
+ feat(database): 新增基于配置的 R2DBC 分片规则与 SpEL 行内算法。

### Bug 修复
+ fix(rbac): 保护系统保留角色身份，防止跨租户租户角色继承。
+ fix(tenant): 强制部门边界约束。
+ fix(resource): 保护文件管理器元数据。
+ fix(message): 会话与广播访问按作用域隔离，缓存按作用域与用户隔离。
+ fix(message): 强制活跃租户成员身份与实时作用域消息访问。
+ fix(message): 停用会话访问并剔除未读计数，撤销过期服务台访问。
+ fix(message): 根据身份隔离收件箱会话，按对方成员关系拆分组织内部/外部会话。
+ fix(message): 广播读取幂等并校验广播读取作用域。
+ fix(message): 收件箱更新原子化，拒绝不可用消息目标与自我私信。
+ fix(message): 会话历史查询参数校验，切换身份标签页或租户时重置会话面板。
+ fix(web): 全局禁用 SWR revalidateOnFocus 以消除标签页聚焦时的重取风暴，并在设置写入后主动刷新系统集成快照。

### 其他
+ style(web): 让 NotificationCenter 透传 className/style，由调用方掌控框架与背景。

---

## v1.14.1

2026-08-08

### 新功能
+ feat(observability): 接入 Elastic APM 链路追踪与 ECS 日志采集，支持自动追踪 @Service / @Controller 方法、增强调用链堆栈、透传 W3C 追踪头并允许调用方自定义分布式事务名。
+ feat(observability): 新增分布式事务记录器与 APM 埋点。
+ feat(database): 实现跨分片的分布式事务（PostgreSQL 2PC）与分片路由能力，支持基于 reactor context 的数据源路由和分片元数据。
+ feat(resource): 完善文件上传状态生命周期，支持阿里云 OSS 分片上传。

### Bug 修复
+ fix(database): 修复 2PC 连接持有器并发获取竞态导致的连接池泄漏，并确保分布式事务提交/回滚后归还连接。
+ fix(database): 移除阻塞式连接获取，使用 Flux.concat 保留 2PC 操作中的 traceId。
+ fix(observability): 将 APM 事务传入 Reactor Context 作为父 span，并修复 suspend 追踪绕过 @Transactional 代理的问题。
+ fix(resource): 改用 MIME 推导的扩展名替代上传方扩展名校验，并保证文件服务缓存的并发安全。
+ fix(auth,user): 加锁租户 OAuth 账号绑定，防止 OAuth2 登录成功重复处理。
+ fix(mail): 异步日志中保留用户与租户上下文。
+ fix(shared): 防止重复的响应式订阅。

### 其他
+ refactor(database): 将分片与 2PC 抽取为独立的 crystal-database 模块，并改用 ShardingSphere 风格的延迟连接架构。
+ refactor(shared): 拆分 R2DBC 基础设施配置，将 SQL 软删除抽取为可插拔的拦截器链。

---

## v1.13.5

2026-08-05

### 新功能
+ feat(resource): 扩展资源存储能力，支持基础上传路径、预签名、复制链接及火山引擎 TOS 分片上传。

### Bug 修复
+ fix(auth): 加强登录与 OAuth 绑定保护，防止用户名枚举并支持一次性令牌。
+ fix(auth,user,tenant): 用户或成员删除、密码或邮箱重置后使现有 JWT 失效。
+ fix(security): 增加出站 URL 白名单，关闭租户可控的 Lark SSRF。
+ fix(sharding,shared): 增加 Snowflake 槽位租约，并在操作期间续租分布式锁。
+ fix(tenant): 串行化成员和邀请配额检查，防止并发绕过。
+ fix(cache,tenant): 将写后缓存清理延迟到事务提交后，并及时失效成员 profile 缓存。
+ fix(user,tenant): 防止重复邮箱，并限制 profile 敏感字段在目标租户范围内。
+ fix(resource): 根据检测到的 MIME 类型校验上传文件扩展名。

---


2026-08-04

### 新功能
+ feat(resource): 新增基于 scope 的文件可见性控制与签名 URL。
+ feat(shared): 启动时校验嵌套租户资源归属，发现配置问题立即失败。
+ feat(user): 为邮箱验证码发送增加限流，防止邮件轰炸。
+ feat(auth): 为登录增加限流，防止暴力破解。

### Bug 修复
+ fix(tenant): 成员停用时强制登出其用户，收紧租户资源归属校验。
+ fix(approval): 修正审批人只读规则与租户归属校验，拒绝任务和实例的越权列表查询。
+ fix(rbac): 按租户隔离 authority 缓存，防止跨租户权限提升。
+ fix(backend): 加固系统初始化流程。
+ fix(user): 在重置邮箱前校验邮箱唯一性，并统一 OAuth 绑定凭证错误以阻止用户名枚举。
+ fix(resource): 通过文件魔数校验文件类型。
+ fix(monitor): 将 actuator 移至仅内部可访问的管理端口。
+ fix(web): 修复前端编译错误，并仅在开发环境记录请求与响应日志。

### 其他
+ chore(system): 调整系统模块包结构。

---


2026-08-02

### 新功能
+ feat(user): 新增账号封禁与启用/禁用管理。
+ feat(auth): 支持基于 issuedAt 的强制登出使 token 失效。
+ feat(rbac): 角色权限分配以 PermissionTreeTable 替代 Transfer，支持多种选择模式与重新设计的布局。
+ feat(approval): 新增绑定系统/租户字典的 DICT 表单字段类型。
+ feat(table): 列设置支持拖拽排序并可重置。

### Bug 修复
+ fix(auth): 封禁/禁用检查收敛至 buildLoginSuccessResponse，封堵 OAuth 绕过。
+ fix(security): 通过 BaseManagerService.query 的 id 短路封堵跨租户 IDOR。
+ fix(shared): 敏感列标记 @NotQueryable，阻止盲探。
+ fix(tenant): 创建租户成员时确保其必有 profile。

### 其他
+ style(approval): 实例 id 标签宽度自适应，定义列表新增 id 标签。

---

## v1.13.2

2026-08-01

### 新功能
+ feat(rbac): 新增权限树视图，合并 Monitor 权限声明。
+ feat(rbac): 403 响应以结构化弹窗形式渲染。
+ feat(audit): 新增自定义 @Audit 注解，并应用于现有审计切面。
+ feat(resource): 新增基于规则的存储提供商路由引擎及模拟端点。

### Bug 修复
+ fix(rbac): 修正租户权限授权层级错误。

### 其他
+ chore: 移除 superpower 插件。
+ chore(audit): 修复审计文档错误。

---

## v1.13.1

2026-07-30

### 新功能
+ feat(approval): 新增审批流表单模块，支持字段定义、分组、校验规则与节点级可见性覆盖。
+ feat(approval): 新增审批流实例查看器与实例级表单 schema 快照。

### Bug 修复
+ fix(shared): 修复 QueryNode → Criteria 转换时数字字符串未转为 Long 的问题。

### 其他
+ refactor(approval): 编辑器右侧面板与表单设计器 UX 优化。

---

## v1.13.0

2026-07-29

### 新功能
+ feat(shared): 新增分布式锁工具与安全响应头。
+ feat(shared): QueryNode 过滤器新增可查询字段白名单。
+ feat(rbac): 拆分租户生命周期字段至 ACTION_TENANT_LIFECYCLE_UPDATE 权限。
+ feat(user): 支持刷新用户 authorities 缓存。

### Bug 修复
+ fix(security): 收紧 scope/ownership 校验，移除静默鉴权回退。
+ fix(security): 加固邮箱验证码，防止暴力破解。
+ fix(security): 用户密码在 JSON 序列化与日志中隐藏。
+ fix(security): 关键流程通过分布式锁串行化，避免竞态。
+ fix(rbac): 阻止越权分配角色与权限。
+ fix(rbac): 对租户角色权限与成员角色启用租户 scope 白名单校验。
+ fix(tenant): 部门成员创建/更新时校验成员 scope 归属。
+ fix(tenant): 拒绝已过期的租户邀请。
+ fix(message-channel): resolveConfig 中校验 scope 归属。
+ fix(mail): 模板占位符替换前进行 HTML 转义。
+ fix(resource): 校验解析后的路径必须在 baseDirectory 内。
+ fix(settings): 敏感设置值不再写入应用日志。
+ fix(settings): 保存时避免掩码值覆盖真实密文。
+ fix(sdk): 校验错误信息中屏蔽敏感设置值。
+ fix(shared): LoggerFilter debug 输出隐藏敏感请求头与请求体字段。

### 其他
+ refactor(backend): 三套权限模型统一为 PermissionMatrix。
+ refactor(rbac): 权限命名统一为 4 层前缀模型，并硬性约束命名规范。
+ refactor(shared,starter): 修正跨租户 scope 不匹配的 HTTP 401→403 语义。
+ refactor(shared,audit): 抽取 AbstractManagerController 与 Mutability。
+ refactor(web,shared,approval,mail,auth,system): 统一 Manager URL 为单数 kebab-case。
+ refactor(audit): ManagerAuditLog DTO 迁移到 dto 子包。
+ refactor(web): 权益页面重命名、按类别归组根组件、抽取 TreeDetailLayout。
+ chore(starter): application.yaml 显式白名单 actuator 端点。

---

## v1.12.0

2026-07-16

### 新功能
+ refactor(shared): 重构 ScopedController 体系。
+ refactor(message-channel): 租户消息渠道重构为支持多 scope 的结构。
+ feat(rbac): 前端支持展示 i18n 权限描述。
+ feat(approval): 完善审批流程抄送节点功能。
+ feat(web): 支持查看审批中的流程图。

### Bug 修复
+ fix(web): 修复系统字典项目管理无法进入。
+ fix(approval): 修复我的审批页面内查询超出范围。
+ fix(approval): 修复审批被拒绝后流程依然进行。

---

## v1.11.1

2026-07-01

### 新功能
+ feat(monitor): 支持 Prometheus + Grafana 监控指标。
+ feat(web): 租户权益概览现返回完整视图。
+ feat(system): 可选是否启用审批和租户模块。
+ feat(monitor): 支持区分不同的 Session 类型。

### 其他
+ docs(skills): 修正租户权益相关的 Skill 文档。

---

## v1.11.0

2026-06-19

### 新功能
+ feat(approval): 新增审批流程模块，支持系统/租户隔离的审批。
+ feat(web): 新增审批流程可视化编辑器。

---

## v1.10.0

2026-06-11

### 新功能
+ feat(tenant): 新增系统/租户级别的全局字典编辑。
+ feat(web): 引入 Slate.js 实现渠道消息可视化编辑。

### Bug 修复
+ **fix(tenant): 修复标准化查询控制器没有限定租户范围。**

---

## v1.9.0

2026-06-10

### 新功能
+ refactor(oauth): 将绑定第三方账号与登录逻辑分离。
+ feat(tenant): 成员加入租户时允许通过消息渠道发送消息。
+ feat(tenant): 允许租户成员绑定第三方账号。
+ feat(oauth): 将 OAuth 相关配置迁移到系统设置中。
+ feat(web): 可动态展示已启用的第三方登录按钮。
+ feat(sdk): 支持通过 SDK 访问消息渠道模块提供的 Gateway 函数。

### Bug 修复
+ fix(backend): 强制使用 ReactiveRedisService 并移除旧实现。
+ fix(backend): 缓存高频读取的 JwtKey 避免线程死锁。
+ fix(oauth): 通过第三方账号注册平台账号时检查是否已绑定到其他用户。
+ fix(web): 修复在未同意用户条款时点击第三方登录按钮的提示文本。
+ fix(web): 修正用户个人资料页内第三方账号解绑按钮文本。

### 其他
- docs: 更新 CLAUDE.md 与部分 skills 文档。

---

## v1.8.0

2026-06-06

### 新功能
+ refactor(backend): 新增用户、租户、系统、认证、权限模块。
+ feat(tenant): 将用户加入组织申请和通知设置分为两个组。
+ feat(encrypt): 允许通过 Registry 注册可直接放行的接口。

### 其他
+ docs: 更新项目结构介绍文档。

---

## v1.7.2

2026-06-05

### 新功能
+ feat(web): 新增消息渠道配置预设模板。
+ feat(web): 租户设置页面渲染 Tabs 标题栏。

### 其他
+ chore(backend): 启动后在控制台打印 CrystalFramework 横幅。
+ chore(web): 消息渠道编辑弹窗关联渠道类型与配置文件。
+ chore(docker): 将 Dockerfile 改为仅复制构建产物，提高构建速度。

---

## v1.7.1 (breaking)

2026-06-05

### 新功能
+ feat(tenant): 新增租户消息渠道管理功能。
+ feat(web): 新增租户消息渠道管理页面。
+ feat(settings): 设置项添加四种（字符串、整数、小数、布尔值）数组数据类型。
+ refactor(sdk): 统一系统设置与租户设置的数据类型。
+ refactor(web): 统一系统设置与租户设置的相关插件注册函数。

### Bug 修复
+ fix(backend): 修复后端 Dockerfile 构建脚本。

### 其他
+ docs: 更新系统设置与租户设置相关文档。
+ chore(web): 由 esbuild 改为使用 terser 构建。

---

## v1.7.0

2026-06-03

### 新功能
+ feat(tenant): 新增租户设置模块。
+ feat(tenant): 新增新成员加入邮件模板与通知，
+ feat(web): 系统设置新增发送测试邮件/消息按钮。
+ feat(web): 新增百度地图 SDK 支持，租户地址可视化选择。
+ feat(web): 运行租户自行编辑其角色权限。

---

## v1.6.2

2026-06-02

### 新功能
+ feat(msg-channel): 新增消息渠道模块并支持邮件与飞书消息。
+ feat(web): 系统设置支持自定义渲染器。

### 其他
+ docs: 更新项目结构文档。

---

## v1.6.1

2026-06-01

### 新功能
+ feat(web): 新增可编辑的租户权益概览页面。
+ feat(web): 新增可编辑的租户权益总览页面。

### Bug 修复
+ fix(tenant): 修复租户邀请码创建功能不受限制。

### 其他
+ docs: 更新 Controller 相关文档。

---

## v1.6.0

2026-05-30

### 新功能
+ feat(tenant): 新增套餐权益模块，支持细粒度控制租户权益。
+ feat(web): 权益项管理/权益取值 Modal 表单根据权益类型动态切换输入控件。
+ feat(web): 套餐权益配置页面添加只读保护模式。
+ feat(web): 套餐权益配置、套餐类型管理页面支持高级组合条件搜索。
+ feat(web): 新组件 TenantInvitationFlow 邀请加入组织。

### Bug 修复
+ fix(test): mock 用户重复导致 `User testuser already exists` 错误。
+ fix(test): 修复测试类通过 getTestClassInstance() 获取 ApplicationContext 失败。

### 其他
+ docs: 新增 CLAUDE.md 三级优先级规则。

---

## v1.5.2

2026-05-28

+ refactor(system & monitor): 将系统统计数据迁移到 monitor 模块。
+ chore(shared): 后端统一使用通用配置类。
+ feat(schedule): @CronTaskExecutor 的 cron 表达式支持通过 Spring 动态计算。
+ feat(monitor): 新增定时任务，每天清理 14 天前的系统监控数据。
+ feat(sdk): 新增 Registry 预检查，防止重复注册引发未知问题。
+ feat(system): 新增系统公告数据表以及相关代码。
+ feat(web): 重新布局仪表盘页面并添加公告列表。

## v1.5.1

2026-05-28

+ feat(web): 高级筛选组件提供自定义条件值渲染器。
+ feat(web): 所有 Manager 页面增加全局搜索框与主键 ID 搜索框。
+ feat(web): 新增 EnhancedNumberInput 组件，支持输入长整数。
+ fix(web): 修复页面因 URL 变更而重新加载的问题。
+ fix(web): 修复 URL 查询参数读取/覆盖非 schema 参数。

## v1.5.0

2026-05-27

+ **!refactor: 移除原有的高级搜索并迁移到条件搜索。**
+ feat(backend): 支持高级组合式条件搜索。
+ refactor: 所有 Manager Query 由 GET 请求变更为 POST 请求。
+ feat(web): 支持可视化高级搜索条件编辑器。
+ feat(web): 所有管理页面均适配 URL 查询参数。

## v1.4.0

2026-05-26

+ **!refactor(web): 重新调整前端项目目录结构。**
+ feat(web): 管理登录日志关联页面支持 URL 携带查询参数。
+ chore(web): 调整 rollup 手动分块打包配置。
+ perf(monitor): 优化指标数据存储逻辑，降低数据库 I/O 消耗。
+ perf(web): 按需动态加载语言文件。
+ docs: 添加 CLAUDE.md 文档。

## v1.3.1

2026-05-25

+ fix(web): 禁止关闭最后一个标签页。
+ fix(web): 水印在可上下滚动的页面仅展示有限高度。
+ fix(web): 标签页快捷键行为异常。
+ chore: 新增项目 CLAUDE.md 文档。

## v1.3.0

2026-05-25

+ docs: 新增控制器、Api 响应、异常处理文档。
+ refactor(web): 从 `src/` 分离前端 `extensions` 文件夹。
+ feat(monitor): 将系统监控插件内置到框架内部。

## v1.2.3 (breaking)

2026-05-24

+ **!refactor(sdk): 取消包扫描器且不再支持集成式二次开发。**
+ feat(sdk): 拓展插件化，二次开发的模块将以插件的形式被加载。
+ feat(web): 引入系统监控插件，新增系统监控页面。
+ feat(schedule): `@CronTaskExecutor` 支持开启/关闭日志输出。
+ feat(system): 新增 `SystemMetricsEvent` 事件与 `SystemMetricsCollectTask` 定时任务。
+ refactor(monitor): 新增 crystal-monitor 模块，将系统监控从插件迁移为一级模块。
+ refactor(web): 将系统监控前端页面从 extension 合并为 manager 内建页面。
+ docs: 新增前端页面开发相关文档。

## v1.2.2

2026-05-24

+ feat(web): 新增 `ContextMenu` 组件。
+ feat(web): 管理页面标签页支持右键菜单与快捷键操作。
+ fix: 修复 Sonar Issues 并移除未使用的依赖。

## v1.2.1

2026-05-23

+ refactor(docs): 调整项目文档目录结构。
+ feat(docs): 新增二次开发/源码贡献相关文档。

## v1.2.0

2026-05-23

+ feat(docs): 新增 CrystalFramework 项目文档。
+ feat(web): 前端同步支持插件化。
+ ci(docs): 文档构建与自动发布工作流。

## v1.1.0

2026-05-22

+ feat(sdk): 新增 crystal-sdk 模块，提供包扫描器、RBAC 权限注册、邮件模板注册。
+ ci(backend): 调整 Github Actions 工作流。

## v1.0.0

2026-05-21

+ CrystalFramework 正式发布 v1.0.0 版本~