package com.lovelycatv.crystalframework.rbac.job

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata
import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.registry.TaskRegistry
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ScheduledTaskMetadata(
    name = "CheckAndClearUnrelatedPermissionsTask",
    description = "Check and clean up permissions not associated with any role",
    group = "rbac"
)
class CheckAndClearUnrelatedPermissionsTask(
    private val userPermissionService: UserPermissionManagerService,
    private val userRolePermissionRelationService: UserRolePermissionRelationService
) : ScheduledTask {

    private val logger = LoggerFactory.getLogger(CheckAndClearUnrelatedPermissionsTask::class.java)

    override suspend fun execute(context: TaskExecutionContext): TaskResult {
        val taskName = TaskRegistry.getTaskName(this)
        logger.info("[$taskName] Starting execution, executionId: ${context.executionId}")

        val allPermissions = userPermissionService
            .getRepository()
            .findAll()
            .awaitListWithTimeout()

        val allRolePermissionRelations = userRolePermissionRelationService
            .getRepository()
            .findAll()
            .awaitListWithTimeout()

        val toBeRemovedPermissions = mutableListOf<UserPermissionEntity>()

        allPermissions.forEach { permission ->
            val related = allRolePermissionRelations.filter { it.permissionId == permission.id }
            if (related.isNotEmpty()) {
                logger.debug("√ ${permission.name} is related to these roles: ${related.map { it.roleId }.joinToString()}")
            } else {
                logger.info("× ${permission.name} is about to be removed")
                toBeRemovedPermissions.add(permission)
            }
        }

        userPermissionService.withBatchDeleteEntityContext(toBeRemovedPermissions.map { it.id }) {
            userPermissionService.getRepository().deleteAll(toBeRemovedPermissions)
        }

        val message = "Removed ${toBeRemovedPermissions.size} unrelated permissions"
        logger.info("[$taskName] $message")

        return TaskResult.Success(message = message, data = toBeRemovedPermissions)
    }
}
