package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.tenant.controller.dto.QueryTenantMatesDTO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/member")
class TenantMateController(
    private val tenantMemberService: TenantMemberService,
) {
    @PostMapping("/query-mates")
    suspend fun queryMates(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: QueryTenantMatesDTO,
    ): ApiResponse<*> {
        val tenantId = dto.tenantId.toLongOrNull()
            ?: throw BusinessException("Invalid tenantId: ${dto.tenantId}")
        if (tenantMemberService.getByTenantIdAndUserId(tenantId, userAuthentication.userId) == null) {
            throw ForbiddenException("User ${userAuthentication.userId} is not a member of tenant $tenantId")
        }
        return ApiResponse.success(tenantMemberService.queryTenantMates(tenantId, dto.page, dto.pageSize))
    }
}
