# Change Logs

## v1.3.1

2026-05-25

+ fix(web): Prevent closing the last remaining tab.
+ fix(web): Watermark displayed at limited height on scrollable pages.
+ fix(web): Tab keyboard shortcut behavior anomalies.
+ chore: Add project CLAUDE.md documentation.

## v1.3.0

2026-05-25

+ docs: Add documentation for controllers, API responses, and exception handling.
+ refactor(web): Separate frontend `extensions` folder from `src/`.
+ feat(monitor): Integrate system monitoring plugin into the framework core.

## v1.2.3 (breaking)

2026-05-24

+ **!refactor(sdk): Remove package scanner; no longer support integrated secondary development.**
+ feat(sdk): Extend plugin architecture — secondary development modules are loaded as plugins.
+ feat(web): Introduce system monitoring plugin with a new system monitor page.
+ feat(schedule): `@CronTaskExecutor` now supports toggling log output.
+ feat(system): Add `SystemMetricsEvent` and `SystemMetricsCollectTask` scheduled job.
+ refactor(monitor): Add crystal-monitor module; migrate system monitoring from plugin to first-class module.
+ refactor(web): Merge system monitor frontend page from extension into manager built-in pages.
+ docs: Add documentation for frontend page development.

## v1.2.2

2026-05-24

+ feat(web): Add `ContextMenu` component.
+ feat(web): Manager page tabs now support context menu and keyboard shortcuts.
+ fix: Fix Sonar issues and remove unused dependencies.

## v1.2.1

2026-05-23

+ refactor(docs): Restructure project documentation directory layout.
+ feat(docs): Add documentation for secondary development / source contribution.

## v1.2.0

2026-05-23

+ feat(docs): Add CrystalFramework project documentation.
+ feat(web): Frontend now supports plugin architecture.
+ ci(docs): Documentation build and auto-publish workflow.

## v1.1.0

2026-05-22

+ feat(sdk): Add crystal-sdk module with package scanner, RBAC permission registration, and email template registration.
+ ci(backend): Adjust GitHub Actions workflows.

## v1.0.0

2026-05-21

+ CrystalFramework v1.0.0 official release~
