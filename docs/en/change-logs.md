# Change Logs

## v1.6.0

2026-05-30

### Features
+ feat(tenant): New tier benefit module for fine-grained tenant benefit control.
+ feat(web): Benefit feature/value modals now dynamically switch input controls based on benefit type.
+ feat(web): Benefit feature config page now has read-only protection mode.
+ feat(web): Benefit feature and tier type manager pages now support advanced FilterBuilder search.
+ feat(web): New TenantInvitationFlow component for inviting members to join organization.

### Bug Fixes
+ fix(test): Duplicate mock usernames causing `User testuser already exists` error.
+ fix(test): Fix getTestClassInstance() failing to obtain ApplicationContext in test classes.

### Other
+ docs: Add CLAUDE.md three-tier priority rule.

---

## v1.5.2

2026-05-28

+ refactor(system & monitor): Migrate system statistics to the monitor module.
+ chore(shared): Unify backend common configuration classes.
+ feat(schedule): @CronTaskExecutor cron expressions now support Spring dynamic evaluation.
+ feat(monitor): Add scheduled task to clean system monitoring data older than 14 days.
+ feat(sdk): Add Registry pre-checks to prevent issues from duplicate registration.
+ feat(system): Add system announcement database table and related code.
+ feat(web): Restructure dashboard page layout and add announcement list.

## v1.5.1

2026-05-28

+ feat(web): Advanced filter component now supports custom condition value renderers.
+ feat(web): All manager pages add a global search box and primary key ID search box.
+ feat(web): Add `EnhancedNumberInput` component with support for long integer input.
+ fix(web): Fix page reloading triggered by URL changes.
+ fix(web): Fix URL query params reading/overwriting non-schema parameters.

## v1.5.0

2026-05-27

+ **!refactor: Remove legacy advanced search and migrate to condition-based search.**
+ feat(backend): Support advanced composable condition-based search.
+ refactor: All Manager Query requests changed from GET to POST.
+ feat(web): Support visual advanced search condition editor.
+ feat(web): All manager pages now support URL query parameters.

## v1.4.0

2026-05-26

+ **!refactor(web): Restructure frontend project directory layout.**
+ feat(web): Manager login log related pages now support URL query parameters.
+ chore(web): Adjust Rollup manual chunk splitting configuration.
+ perf(monitor): Optimize metrics data storage logic to reduce database I/O overhead.
+ perf(web): Dynamically load language files on demand.
+ docs: Add CLAUDE.md documentation.

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
