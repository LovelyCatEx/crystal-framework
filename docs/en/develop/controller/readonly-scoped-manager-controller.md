# ReadonlyScopedManagerController

The read-only variant of [`StandardScopedManagerController`](./scoped-manager-controller). Query endpoints inherit; mutations (create / update / delete) are overridden at the business layer to return 403. For "system-generated, dual-scope-viewable, user-immutable" resources.

## Applicable scenarios

- Approval flow instances (visible in both system and tenant admin, but users cannot directly modify instances)
- Approval task lists
- Other system-generated, dual-scope, read-only records

Other scenarios:

- Mutable dual-scope resource → [StandardScopedManagerController](./scoped-manager-controller)
- Global read-only (no scope) → [ReadonlyManagerController](./readonly-manager-controller)

## Endpoints

Inherited from `StandardScopedManagerController`; mutations return 403 at the business layer:

| HTTP | Path | Behavior |
|---|---|---|
| GET | `/list?scope=&scopeId=` | Normal |
| POST | `/query` | Normal |
| POST | `/create` | 403 Forbidden |
| POST | `/update` | 403 Forbidden |
| POST | `/delete` | 403 Forbidden |

## Use the `Triad.readonly(...)` factory

`ScopedPermissionTriad` has 12 slots; read-only cases only need 3 read slots. The `readonly` factory fills the other 9 CRUD slots with `NEVER_GRANTED` (a string that can never be granted), ensuring deny-by-default even if code accidentally bypasses ReadonlyScoped and looks up permissions directly:

```kotlin
permissions = ScopedPermissionTriad.readonly(
    superRead      = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead  = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

## Usage steps

Using approval flow instance (read-only) as an example.

### 1. Entity

Use a Scoped-family entity (extends `BaseScopedEntity`):

```kotlin
@Table("approval_flow_instance")
class ApprovalFlowInstanceEntity(
    id: Long = 0,
    var definitionId: Long = 0,
    var initiatorId: Long = 0,
    var status: Int = 0,
    scope: Int = 0,
    scopeId: Long = 0,
) : BaseScopedEntity(id, scope, scopeId)
```

### 2. Service

Extends `BaseScopedManagerService`:

```kotlin
interface ApprovalFlowInstanceManagerService : BaseScopedManagerService<
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO
>
```

### 3. DTOs

Even though mutations are never used, all four DTOs must be provided. The Delete DTO may reuse the base:

```kotlin
class ManagerCreateApprovalFlowInstanceDTO(val placeholder: String = "") : Any
class ManagerReadApprovalFlowInstanceDTO(
    page: Int = 1, pageSize: Int = 20, scope: Int = 0, scopeId: Long = 0,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
class ManagerUpdateApprovalFlowInstanceDTO(override val id: Long) : BaseManagerUpdateDTO(id)
// Delete uses the base BaseManagerDeleteDTO directly
```

### 4. Controller

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-instances")
class ManagerApprovalFlowInstanceController(
    managerService: ApprovalFlowInstanceManagerService,
    private val approvalFlowEngine: ApprovalFlowEngine,      // additional deps allowed
) : ReadonlyScopedManagerController<
    ApprovalFlowInstanceManagerService,
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO
>(
    managerService,
    permissions = ScopedPermissionTriad.readonly(
        superRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        systemRead    = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        tenantPemRead = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
    ),
) {

    // Custom endpoints allowed, e.g. "start approval"
    @PostMapping("/start")
    suspend fun start(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: StartApprovalFlowDTO
    ): ApiResponse<*> {
        // custom permission check + business logic
    }
}
```

## Type parameters

Same 7 parameters as [StandardScopedManagerController](./scoped-manager-controller).

## Common override pattern

Read-only endpoints often shape the result by user's permission — for example, regular users see only their own approval instances, admins see all. That logic goes in `buildQueryResponse`:

```kotlin
override suspend fun buildQueryResponse(
    dto: ManagerReadApprovalFlowInstanceDTO,
    userAuthentication: UserAuthentication,
): Any {
    val canReadAll = RbacUtils.hasAnyAuthority(*permissions!!.forScope(scope, READ))
    val effectiveDto = if (canReadAll) dto
                       else dto.copy(query = <append initiator_id filter>)
    return managerService.query(effectiveDto)
}
```

Key points:

- Do not gate "can this user read?" in `checkPermission` — a failed check returns 403 without letting the user through. The strategy is "any logged-in user can read, but what they see depends on their permission"
- Override `checkPermission` to return `operation == READ` (open to all logged-in users), then inject filters inside `buildQueryResponse` — cleaner than rewriting the `query` endpoint

## Notes

- Always use the `readonly(...)` factory for the Triad — manually filling all 12 slots is error-prone
- Custom mutation endpoints must handle their own permission checks — the parent's 403 only covers `/create` / `/update` / `/delete`; new endpoints like `/mark-as-seen` need `@PreAuthorize` or an inline `checkPermission`
- `@ManagerPermissions` has no effect here (same as the Scoped family)
- Delete DTO may reuse the base class (`BaseManagerDeleteDTO`) since the business layer returns 403 anyway — no extra fields needed
