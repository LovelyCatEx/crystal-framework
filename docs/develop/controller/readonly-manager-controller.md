# 只读标准化控制器（ReadonlyManagerController）

[`StandardManagerController`](./standard-manager-controller) 的只读变体。查询端点全部继承，写操作（create / update / delete）由业务层重写返回 403。适用于日志类、系统生成、不允许人工修改的资源。

## 适用场景

- 登录日志、审计日志、邮件发送记录
- 系统自动写入、管理员只能查看

其他场景：

- 允许修改的资源 → [StandardManagerController](./standard-manager-controller)
- 双 scope 的只读资源 → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## 端点

继承自 `StandardManagerController`，写操作在业务层返回 403：

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

`@ManagerPermissions` 中 5 个字段全部填同一个读权限。原因：

- `read` / `readAll` 用读权限符合语义
- `create` / `update` / `delete` 即使 AOP 校验通过（用户持有 `_READ`），业务层重写后仍返回 403——双重防护

## 双重防护是刻意的

即使把写操作权限配为真实存在的 `_CREATE` / `_UPDATE` / `_DELETE`，读操作通过、写操作被业务层挡下——但**不应**这样配置。此类的设计意图是"该资源永远不能被 API 修改"，因此：

- `@ManagerPermissions` 中 5 个字段建议全部填读权限，语义清晰
- 如需开放"给管理员改日志"的接口，应单独写非 Readonly 的 Controller，不要复用此类

## 类型参数

与 [StandardManagerController](./standard-manager-controller) 完全一致的 7 个类型参数——此类继承自 Standard，仅重写 3 个方法。

## 添加自定义端点

`create` / `update` / `delete` 被 override 为 403，但可添加自定义写操作（如"标记日志已读"）：

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_SEND_LOG_READ}')")
@PostMapping("/mark-as-seen")
suspend fun markAsSeen(@RequestParam id: Long): ApiResponse<*> {
    managerService.markAsSeen(id)
    return ApiResponse.success(null)
}
```

自定义端点需自行添加 `@PreAuthorize`，不受父类 403 影响。

## 注意事项

- 4 个 DTO 必须全部提供，CREATE / UPDATE / DELETE 实际不使用，业务上写成最简即可
- `@ManagerPermissions` 中 5 个字段必须全部填写。留空会被 AOP 打 warn 并放行
- `ReadonlyManagerController` 只挡了 API 层——Service 层被内部定时任务等其他调用点触达时仍能修改。若需 DB 层保护要在 DDL 加约束
