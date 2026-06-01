# 更新日志

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