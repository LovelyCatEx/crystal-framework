package com.lovelycatv.crystalframework.rbac.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TenantRoleManagerServiceImplTest(
    @Autowired private val tenantRoleManagerService: TenantRoleManagerService,
) : CrystalFrameworkApplicationTests() {

    @Test
    fun createRejectsParentRoleFromAnotherTenant() {
        withTransactionalRollback("tenant-role-create-foreign-parent") {
            val foreignParent = tenantRoleManagerService.create(
                ManagerCreateTenantRoleDTO(tenantId = FOREIGN_TENANT_ID, name = "Foreign Parent"),
            )

            val exception = runCatching {
                tenantRoleManagerService.create(
                    ManagerCreateTenantRoleDTO(
                        tenantId = OWN_TENANT_ID,
                        name = "Cross Tenant Child",
                        parentId = foreignParent.id,
                    ),
                )
            }.exceptionOrNull()

            assertIs<BusinessException>(exception)
        }
    }

    @Test
    fun updateRejectsParentRoleFromAnotherTenant() {
        withTransactionalRollback("tenant-role-update-foreign-parent") {
            val role = tenantRoleManagerService.create(
                ManagerCreateTenantRoleDTO(tenantId = OWN_TENANT_ID, name = "Own Role"),
            )
            val foreignParent = tenantRoleManagerService.create(
                ManagerCreateTenantRoleDTO(tenantId = FOREIGN_TENANT_ID, name = "Foreign Parent"),
            )

            val exception = runCatching {
                tenantRoleManagerService.update(
                    ManagerUpdateTenantRoleDTO(id = role.id, parentId = foreignParent.id),
                )
            }.exceptionOrNull()

            assertIs<BusinessException>(exception)
            assertEquals(null, tenantRoleManagerService.getByIdOrNull(role.id)?.parentId)
        }
    }

    @Test
    fun createAcceptsParentRoleFromSameTenant() {
        withTransactionalRollback("tenant-role-create-own-parent") {
            val parent = tenantRoleManagerService.create(
                ManagerCreateTenantRoleDTO(tenantId = OWN_TENANT_ID, name = "Parent"),
            )

            val child = tenantRoleManagerService.create(
                ManagerCreateTenantRoleDTO(
                    tenantId = OWN_TENANT_ID,
                    name = "Child",
                    parentId = parent.id,
                ),
            )

            assertNotNull(child)
            assertEquals(parent.id, child.parentId)
        }
    }

    companion object {
        private const val OWN_TENANT_ID = 1L
        private const val FOREIGN_TENANT_ID = 2L
    }
}
