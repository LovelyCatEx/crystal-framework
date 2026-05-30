package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitOverviewDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo.ManagerReadTenantTireBenefitOverviewItemVO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitFeatureManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import jakarta.validation.Valid
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(read = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_READ])
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire/benefit/overview")
class ManagerTenantTireBenefitOverviewController(
    private val managerService: TenantTireBenefitValueManagerService,
    private val featureManagerService: TenantTireBenefitFeatureManagerService,
    private val benefitValueRepository: TenantTireBenefitValueRepository,
) {
    @PostMapping("/query")
    suspend fun queryOverview(
        @RequestBody
        @Valid
        dto: ManagerReadTenantTireBenefitOverviewDTO,
    ): ApiResponse<*> {
        // 1. Paginate features via feature manager service
        val featurePage = featureManagerService.query(
            ManagerReadTenantTireBenefitFeatureDTO(
                page = dto.page,
                pageSize = dto.pageSize,
            )
        )

        // 2. Fetch all bound values for this tire type
        val values = benefitValueRepository.findByTireTypeId(dto.tireTypeId)
            .collectList().awaitFirstOrNull() ?: emptyList()
        val valueMap = values.associateBy { it.featureId }

        // 3. Merge features with their values into overview VOs
        val records = featurePage.records.map { feature ->
            val existingValue = valueMap[feature.id]
            ManagerReadTenantTireBenefitOverviewItemVO(
                featureId = feature.id,
                featureKey = feature.featureKey,
                name = feature.name,
                description = feature.description,
                featureType = feature.featureType,
                defaultValue = feature.defaultValue,
                value = existingValue?.featureValue,
                valueId = existingValue?.id,
                isCustomized = existingValue != null,
            )
        }

        return ApiResponse.success(
            PaginatedResponseData(
                page = featurePage.page,
                pageSize = featurePage.pageSize,
                total = featurePage.total,
                totalPages = featurePage.totalPages,
                records = records,
            )
        )
    }
}
