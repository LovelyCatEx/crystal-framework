# ReadonlyManagerController

The read-only variant of [`StandardManagerController`](./standard-manager-controller). Query endpoints inherit; mutations (create / update / delete) are overridden at the business layer to return 403. For log-like, system-generated, immutable resources.

## Applicable scenarios

- Login logs, audit logs, mail-send records
- Data written automatically by the system; admins only view

Other scenarios:

- Mutable resources → [StandardManagerController](./standard-manager-controller)
- Dual-scope read-only → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## Endpoints

Inherited from `StandardManagerController`; mutations return 403 at the business layer:

| HTTP | Path | Behavior |
|---|---|---|
| GET | `/list` | Normal |
| POST | `/query` | Normal |
| POST | `/create` | 403 Forbidden |
| POST | `/update` | 403 Forbidden |
| POST | `/delete` | 403 Forbidden |

## Usage steps

Using `mail-send-log` as an example.

### 1–4. Entity / Repository / Service / DTOs

Set them up exactly like [StandardManagerController](./standard-manager-controller). Even though mutations always return 403, all four DTOs must still be provided (the generic base requires the type parameters).

```kotlin
class ManagerCreateMailSendLogDTO(
    // Never actually invoked; a minimal definition is fine
    val placeholder: String = "",
)
```

Service extends `CachedBaseManagerService` (same as Standard).

### 5. Controller

```kotlin
@ManagerPermissions(
    read = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    update = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-send-logs")
class ManagerMailSendLogController(
    managerService: MailSendLogManagerService
) : ReadonlyManagerController<
    MailSendLogManagerService,
    MailSendLogRepository,
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
>(managerService)
```

All 5 `@ManagerPermissions` fields use the same read permission. Reasons:

- `read` / `readAll` naturally use the read permission
- Even if AOP lets a mutation through (a user somehow holds `_READ`), the business-layer override still returns 403 — defense in depth

## Defense in depth is deliberate

You could configure mutations with real `_CREATE` / `_UPDATE` / `_DELETE` permissions and still have the business layer block them. But you shouldn't — the design intent is "this resource is API-immutable", so:

- Set all 5 fields to the read permission for clearest semantics
- If you later need "allow admins to edit logs", write a separate non-Readonly Controller — do not repurpose this class

## Type parameters

Same 7 parameters as [StandardManagerController](./standard-manager-controller) — this class extends Standard and overrides 3 methods only.

## Adding custom endpoints

`create` / `update` / `delete` are overridden to 403, but custom mutations can be added (e.g. "mark log as seen"):

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_SEND_LOG_READ}')")
@PostMapping("/mark-as-seen")
suspend fun markAsSeen(@RequestParam id: Long): ApiResponse<*> {
    managerService.markAsSeen(id)
    return ApiResponse.success(null)
}
```

Custom endpoints write their own `@PreAuthorize`; the parent's 403 does not apply.

## Notes

- All 4 DTOs are still required — CREATE / UPDATE / DELETE are unused in practice, so keep them minimal
- All 5 `@ManagerPermissions` fields must be set; empty arrays make AOP log a warn and allow the call
- `ReadonlyManagerController` only blocks the API layer — the Service layer remains callable from internal jobs. For DB-level protection add DDL constraints
