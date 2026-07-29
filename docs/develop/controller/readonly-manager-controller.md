# 只读标准化控制器（ReadonlyManagerController）

[`StandardManagerController`](./standard-manager-controller) 的只读变体。查询端点全部继承，写操作（create / update / delete）由业务层重写返回 403。适用于日志类、系统生成、不允许人工修改的资源。

## 适用场景

- 登录日志、审计日志、邮件发送记录
- 系统自动写入、管理员只能查看

其他场景：

- 允许修改的资源 → [StandardManagerController](./standard-manager-controller)
- 双 scope 的只读资源 → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## 端点

继承自 `StandardManagerController`，写操作由 `Mutability.READ_ONLY` 挡下返回 403：

| HTTP | 路径 | 行为 |
|---|---|---|
| GET | `/list` | 正常返回 |
| POST | `/query` | 正常返回 |
| POST | `/create` | 403 Forbidden |
| POST | `/update` | 403 Forbidden |
| POST | `/delete` | 403 Forbidden |

## 使用步骤

以 `mail-send-log`（邮件发送记录）为例。

### 1–4. Entity / Repository / Service / DTO

与 [StandardManagerController](./standard-manager-controller) 完全一致。即使写操作永远返回 403，四个 DTO 依然必须提供（泛型基类约束要求参数就位）。

```kotlin
class ManagerCreateMailSendLogDTO(
    // 实际不会被调用，字段可以最简
    val placeholder: String = "",
)
```

Service 继承 `CachedBaseManagerService`（与 Standard 相同）。

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

`PermissionMatrix.systemOnlyReadonly(...)` 只需填 `systemRead`（可选补 `superRead`）—— 工厂自动把 CUD 4 位填成 `NEVER_GRANTED`、tenant 层 8 位填成 `NOT_APPLICABLE`。

## 双重防护是刻意的

`Mutability.READ_ONLY` 在业务层直接封死 CUD 三个端点，`PermissionMatrix.systemOnlyReadonly(...)` 又在权限层用 `NEVER_GRANTED` 兜底 —— 两层独立防线：

- 业务层：`Mutability.READ_ONLY` 让 CUD 端点抛 `ForbiddenException`
- 权限层：即使有人绕开业务层直接查 `matrix.systemFor(CREATE)`，得到的是 `NEVER_GRANTED`，`hasAnyAuthority` 恒为 false

不建议手工把 CUD 的权限填成真实的写权限 —— 此类的设计意图就是"该资源永远不能被 API 修改"。如需开放"给管理员改日志"的接口，应单独写非 Readonly 的 Controller，不要复用此类。

## 类型参数

与 [StandardManagerController](./standard-manager-controller) 完全一致的 7 个类型参数——此类继承自 Standard，仅重写 3 个方法。

## 添加自定义端点

`create` / `update` / `delete` 三个端点被 `Mutability.READ_ONLY` 挡下返回 403，但可添加自定义写操作（如"标记日志已读"）：

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ}')")
@PostMapping("/mark-as-seen")
suspend fun markAsSeen(@RequestParam id: Long): ApiResponse<*> {
    managerService.markAsSeen(id)
    return ApiResponse.success(null)
}
```

自定义端点需自行添加 `@PreAuthorize`，不受父类 403 影响。

## 注意事项

- 4 个 DTO 必须全部提供，CREATE / UPDATE / DELETE 实际不使用，业务上写成最简即可
- 权限声明必须使用 `PermissionMatrix.systemOnlyReadonly(...)`；老的 `@ManagerPermissions` 注解已弃用，见 [权限模型迁移指南](./permission-migration)
- `ReadonlyManagerController` 只挡了 API 层——Service 层被内部定时任务等其他调用点触达时仍能修改。若需 DB 层保护要在 DDL 加约束
