# Change Logs

## Announcement

::: danger
1. Versions v1.5.* ~ v1.9.* contain critical security vulnerabilities. Do not use them. See v1.10.0 change log for details.
:::

## v1.12.0

2026-07-16

### Features
+ refactor(shared): Restructure the ScopedController hierarchy.
+ refactor(message-channel): Restructure tenant message channels to support multiple scopes.
+ feat(rbac): Support displaying i18n permission descriptions on the frontend.
+ feat(approval): Enhance the CC node in approval flows.
+ feat(web): Support viewing the flow diagram of in-progress approvals.

### Bug Fixes
+ fix(web): Fix inaccessible system dictionary item management page.
+ fix(approval): Fix out-of-scope query on the My Approvals page.
+ fix(approval): Fix approval flow continuing after being rejected.

---

## v1.11.1

2026-07-01

### Features
+ feat(monitor): Support Prometheus + Grafana monitoring metrics.
+ feat(web): Tenant benefit overview now returns the full view.
+ feat(system): Optionally toggle the approval and tenant modules.
+ feat(monitor): Distinguish between different session types.

### Others
+ docs(skills): Update the tenant-benefit skill documentation.

---

## v1.11.0

2026-06-19

### Features
+ feat(approval): Add approval flow module, supporting system/tenant-isolated approvals.
+ feat(web): Add visual approval flow editor.

---

## v1.10.0

2026-06-11

### Features
+ feat(tenant): Add system/tenant-level global dictionary editing.
+ feat(web): Introduce Slate.js for visual message channel editor.

### Bug Fixes
+ fix(tenant): Fix standardized query controller not scoping by tenant.

---

## v1.9.0

2026-06-10

### Features
+ refactor(oauth): Decouple OAuth binding flow from login flow.
+ feat(tenant): Allow sending messages via message channels when a member joins the tenant.
+ feat(tenant): Allow tenant members to bind third-party accounts.
+ feat(oauth): Migrate OAuth configuration to system settings.
+ feat(web): Dynamically display enabled third-party login buttons.
+ feat(sdk): Support accessing message channel module Gateway functions via SDK.

### Bug Fixes
+ fix(backend): Enforce usage of ReactiveRedisService and remove legacy implementation.
+ fix(backend): Cache frequently-read JwtKey to avoid thread deadlock.
+ fix(oauth): Check for conflicting bindings when registering via OAuth account.
+ fix(web): Fix i18n of OAuth login agreement warning text.
+ fix(web): Correct i18n key for OAuth unbind button in user profile page.

### Other
- docs: Update CLAUDE.md and related skills documentation.

---

## v1.8.0

2026-06-06

### Features
+ refactor(backend): Added user, tenant, system, auth, and rbac modules.
+ feat(tenant): Split the organization join request and notification settings into two groups.
+ feat(encrypt): Allow registering directly-passable endpoints via the Registry.

### Other
+ docs: Updated the project structure introduction document.

---

## v1.7.2

2026-06-05

### Features
+ feat(web): Added preset templates for message channel config.
+ feat(web): Tenant settings page now renders Tabs header.

### Other
+ chore(backend): Print CrystalFramework banner in the console after startup.
+ chore(web): Message channel edit modal now links channel type with config presets.
+ chore(docker): Switch Dockerfiles to copy build artifacts only for faster builds.

---

## v1.7.1 (breaking)

2026-06-05

### Features
+ feat(tenant): Added tenant message channel management.
+ feat(web): Added tenant message channel management pages.
+ feat(settings): Added four array data types (string, integer, decimal, boolean) for setting items.
+ refactor(sdk): Unified data types of system settings and tenant settings.
+ refactor(web): Unified plugin registration functions for system settings and tenant settings.

### Bug Fixes
+ fix(backend): Fixed backend Dockerfile build script.

### Other
+ docs: Updated documentation for system settings and tenant settings.
+ chore(web): Switched build from esbuild to terser.

---

## v1.7.0

2026-06-03

### Features
+ feat(tenant): Added tenant settings module.
+ feat(tenant): Added mail template and notification for new member join.
+ feat(web): Added send-test-email/message buttons in system settings.
+ feat(web): Added Baidu Map SDK support and visual address picker for tenant address.
+ feat(web): Allow tenants to edit their own role permissions.

---

## v1.6.2

2026-06-02

### Features
+ feat(msg-channel): Added message channel module with mail and Lark message support.
+ feat(web): System settings support custom renderers.

### Other
+ docs: Updated project structure documentation.

---

## v1.6.1

2026-06-01

### Features
+ feat(web): Added editable tenant benefit overview page.
+ feat(web): Added editable tenant benefit summary page.

### Bug Fixes
+ fix(tenant): Fixed tenant invitation code creation not being restricted by INVITATION_ENABLED benefit.

### Other
+ docs: Updated Controller-related documentation.

---

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
