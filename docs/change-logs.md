# 更新日志

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