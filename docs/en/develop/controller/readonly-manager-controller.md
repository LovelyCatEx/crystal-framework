# ReadonlyManagerController

The read-only variant of [`StandardManagerController`](./standard-manager-controller). Query endpoints inherit; mutations (create / update / delete) are overridden at the business layer to return 403. For log-like, system-generated, immutable resources.

## Applicable scenarios

- Login logs, audit logs, mail-send records
- Data written automatically by the system; admins only view

Other scenarios:

- Mutable resources → [StandardManagerController](./standard-manager-controller)
- Dual-scope read-only → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## Endpoints

Inherited from `StandardManagerController`; mutations are blocked by `Mutability.READ_ONLY` and return 403:

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
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-send-log")
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
>(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
    ),
)
```

`PermissionMatrix.systemOnlyReadonly(...)` just needs `systemRead` (optionally `superRead`) — the factory fills the 4 CUD slots with `NEVER_GRANTED` and the 8 tenant slots with `NOT_APPLICABLE`.

## Defense in depth is deliberate

`Mutability.READ_ONLY` seals off CUD at the business layer; `PermissionMatrix.systemOnlyReadonly(...)` seals them again at the permission layer with `NEVER_GRANTED` — two independent lines of defense:

- Business layer: `Mutability.READ_ONLY` makes CUD endpoints throw `ForbiddenException`
- Permission layer: even if someone bypasses the business layer and calls `matrix.systemFor(CREATE)`, the result is `NEVER_GRANTED` and `hasAnyAuthority` is always false

Do not manually put real mutation permissions in the CUD slots — the intent is "this resource is never modifiable via API". If you later need "allow admins to edit logs", write a separate non-Readonly Controller — do not repurpose this class.

## Type parameters

Same 7 parameters as [StandardManagerController](./standard-manager-controller) — this class extends Standard and overrides 3 methods only.

## Adding custom endpoints

`create` / `update` / `delete` are blocked by `Mutability.READ_ONLY` and return 403, but custom mutations can be added (e.g. "mark log as seen"):

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ}')")
@PostMapping("/mark-as-seen")
suspend fun markAsSeen(@RequestParam id: Long): ApiResponse<*> {
    managerService.markAsSeen(id)
    return ApiResponse.success(null)
}
```

Custom endpoints write their own `@PreAuthorize`; the parent's 403 does not apply.

## Notes

- All 4 DTOs are still required — CREATE / UPDATE / DELETE are unused in practice, so keep them minimal
- Declare permissions via `PermissionMatrix.systemOnlyReadonly(...)`; the legacy `@ManagerPermissions` annotation is deprecated — see the [Permission Model Migration Guide](./permission-migration)
- `ReadonlyManagerController` only blocks the API layer — the Service layer remains callable from internal jobs. For DB-level protection add DDL constraints
