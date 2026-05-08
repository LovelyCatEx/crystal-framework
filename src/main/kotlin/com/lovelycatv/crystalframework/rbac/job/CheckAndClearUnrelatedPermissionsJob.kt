package com.lovelycatv.crystalframework.rbac.job

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor
import com.aizuda.snailjob.client.job.core.dto.JobArgs
import com.aizuda.snailjob.common.log.SnailJobLog
import com.aizuda.snailjob.model.dto.ExecuteResult
import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component


@Component
@JobExecutor(name = "CheckAndClearUnrelatedPermissionsJob")
class CheckAndClearUnrelatedPermissionsJob(
    private val userPermissionService: UserPermissionManagerService,
    private val userRolePermissionRelationService: UserRolePermissionRelationService
) {
    fun jobExecute(jobArgs: JobArgs): ExecuteResult {
        return runBlocking {
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
                    SnailJobLog.REMOTE.debug("√ ${permission.name} is related to these roles: ${related.map { it.roleId }.joinToString()}")
                } else {
                    SnailJobLog.REMOTE.info("× ${permission.name} is about to be removed")
                    toBeRemovedPermissions.add(permission)
                }
            }

            userPermissionService.withBatchDeleteEntityContext(toBeRemovedPermissions.map { it.id }) {
                userPermissionService.getRepository().deleteAll(toBeRemovedPermissions)
            }

            SnailJobLog.REMOTE.info("All permissions has been removed.")

            ExecuteResult.success(toBeRemovedPermissions)
        }
    }
}