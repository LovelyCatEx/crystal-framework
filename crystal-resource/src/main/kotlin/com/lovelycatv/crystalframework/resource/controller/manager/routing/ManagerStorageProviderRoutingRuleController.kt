package com.lovelycatv.crystalframework.resource.controller.manager.routing

import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerDeleteStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerReadStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerReorderStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerUpdateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderRoutingRuleManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider-routing-rule")
class ManagerStorageProviderRoutingRuleController(
    managerService: StorageProviderRoutingRuleManagerService
) : StandardManagerController<
        StorageProviderRoutingRuleManagerService,
        StorageProviderRoutingRuleRepository,
        StorageProviderRoutingRuleEntity,
        ManagerCreateStorageProviderRoutingRuleDTO,
        ManagerReadStorageProviderRoutingRuleDTO,
        ManagerUpdateStorageProviderRoutingRuleDTO,
        ManagerDeleteStorageProviderRoutingRuleDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_DELETE.name,
    ),
) {
    /** Drag-and-drop priority reorder: writes `priority` = index within [dto.orderedIds]. */
    @PostMapping("/reorder")
    suspend fun reorder(
        userAuthentication: UserAuthentication,
        @Valid
        @RequestBody
        dto: ManagerReorderStorageProviderRoutingRuleDTO
    ): ApiResponse<*> {
        authorize(ManagerAction.UPDATE, userAuthentication)
        managerService.reorder(dto.orderedIds)
        return ApiResponse.success(null)
    }
}
