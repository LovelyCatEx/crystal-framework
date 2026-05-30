package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo.ManagerReadTenantTireBenefitOverviewItemVO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(read = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_READ])
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire/benefit/overview")
class ManagerTenantTireBenefitOverviewController(
    private val managerService: TenantTireBenefitValueManagerService,
) {

    @GetMapping
    suspend fun getOverview(
        @RequestParam tireTypeId: Long,
    ): ApiResponse<List<ManagerReadTenantTireBenefitOverviewItemVO>> {
        return ApiResponse.success(managerService.getOverview(tireTypeId))
    }
}
