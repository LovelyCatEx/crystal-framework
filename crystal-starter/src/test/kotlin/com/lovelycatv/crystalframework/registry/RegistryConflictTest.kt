package com.lovelycatv.crystalframework.registry

import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext
import com.lovelycatv.crystalframework.schedule.registry.TaskRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRoleDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegistryConflictTest {

    // -------------------------------------------------------------------------
    // SystemRbacRegistry
    // -------------------------------------------------------------------------

    @Test
    fun systemRbacRegistryDuplicatePermission() {
        val registry = SystemRbacRegistry()
        val permission = SystemRbacPermissionDeclaration.action(name = "dashboard.read")
        registry.permission(permission)

        val ex = assertThrows<IllegalStateException> {
            registry.permission(permission.copy())
        }
        println("[SystemRbacRegistry] duplicate permission detected: ${ex.message}")
    }

    @Test
    fun systemRbacRegistryDuplicateRole() {
        val registry = SystemRbacRegistry()
        val role = SystemRoleDeclaration(name = "ROLE_ADMIN")
        registry.role(role)

        val ex = assertThrows<IllegalStateException> {
            registry.role(role.copy())
        }
        println("[SystemRbacRegistry] duplicate role detected: ${ex.message}")
    }

    // -------------------------------------------------------------------------
    // TenantRbacRegistry
    // -------------------------------------------------------------------------

    @Test
    fun tenantRbacRegistryDuplicatePermission() {
        val registry = TenantRbacRegistry()
        val permission = TenantPermissionDeclaration(name = "tenant.member.read", type = TenantPermissionType.ACTION)
        registry.permission(permission)

        val ex = assertThrows<IllegalStateException> {
            registry.permission(permission.copy())
        }
        println("[TenantRbacRegistry] duplicate permission detected: ${ex.message}")
    }

    @Test
    fun tenantRbacRegistryDuplicateRole() {
        val registry = TenantRbacRegistry()
        val role = TenantRoleDeclaration(name = "ROLE_OWNER")
        registry.role(role)

        val ex = assertThrows<IllegalStateException> {
            registry.role(role.copy())
        }
        println("[TenantRbacRegistry] duplicate role detected: ${ex.message}")
    }

    // -------------------------------------------------------------------------
    // SystemSettingsRegistry
    // -------------------------------------------------------------------------

    @Test
    fun systemSettingsRegistryDuplicateKey() {
        val registry = SystemSettingsRegistry()
        val declaration = SettingsItemDeclaration(
            key = "mail.smtp.host",
            valueType = SettingsItemValueType.STRING,
            defaultValue = "127.0.0.1"
        )
        registry.setting(declaration)

        val ex = assertThrows<IllegalStateException> {
            registry.setting(declaration.copy())
        }
        println("[SystemSettingsRegistry] duplicate setting key detected: ${ex.message}")
    }

    // -------------------------------------------------------------------------
    // TaskRegistry
    // -------------------------------------------------------------------------

    @Test
    fun taskRegistryDuplicateTaskName() {
        @ScheduledTaskMetadata(name = "DuplicateTask")
        class TaskA : ScheduledTask {
            override suspend fun execute(context: TaskExecutionContext): TaskResult =
                TaskResult.Success("ok")
        }

        @ScheduledTaskMetadata(name = "DuplicateTask")
        class TaskB : ScheduledTask {
            override suspend fun execute(context: TaskExecutionContext): TaskResult =
                TaskResult.Success("ok")
        }

        val registry = TaskRegistry()
        registry.register(TaskA())

        val ex = assertThrows<IllegalStateException> {
            registry.register(TaskB())
        }
        println("[TaskRegistry] duplicate task name detected: ${ex.message}")
    }
}
