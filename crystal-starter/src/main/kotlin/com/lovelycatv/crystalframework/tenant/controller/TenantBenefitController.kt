package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/benefits")
class TenantBenefitController(
    private val tenantBenefitService: TenantBenefitService,
    private val tenantService: TenantService,
) {
    @GetMapping
    suspend fun getMyBenefits(authentication: UserAuthentication): ApiResponse<Map<String, String>> {
        val tenantId = authentication.assertTenantIdNotNull()
        val tenant = tenantService.getByIdOrNull(tenantId) ?: return ApiResponse.badRequest("tenant not found")
        val benefits = tenantBenefitService.getAllBenefitsForTireType(tenant.tireTypeId)
        return ApiResponse.success(benefits)
    }
}
