package com.lovelycatv.crystalframework.message.controller

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.message.constants.MessageConstants
import com.lovelycatv.crystalframework.message.controller.vo.BroadcastInboxVO
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.sdk.message.config.UserTenantProvider
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * End-user broadcast inbox (read-diffusion): the unread badge count and the paginated history
 * (read + expired included) — audience membership is resolved live against the user's tenants —
 * plus a lazy read marker recorded on open. Every endpoint requires an authenticated user.
 */
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/broadcast")
class BroadcastController(
    private val broadcastService: BroadcastService,
    private val userTenantProvider: UserTenantProvider,
) {
    @GetMapping("/unread-count")
    suspend fun unreadCount(userAuthentication: UserAuthentication): ApiResponse<*> {
        val tenantIds = userTenantProvider.tenantIdsOf(userAuthentication.userId)
        return ApiResponse.success(broadcastService.unreadCount(userAuthentication.userId, tenantIds))
    }

    @GetMapping("/history")
    suspend fun history(
        userAuthentication: UserAuthentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ApiResponse<*> {
        val safePage = page.coerceAtLeast(1)
        val safePageSize = pageSize.coerceIn(1, MessageConstants.MAX_BROADCAST_PAGE_SIZE)
        val tenantIds = userTenantProvider.tenantIdsOf(userAuthentication.userId)
        val result = broadcastService.listHistory(userAuthentication.userId, tenantIds, safePage, safePageSize)
        return ApiResponse.success(
            PaginatedResponseData(
                page = result.page,
                pageSize = result.pageSize,
                total = result.total,
                totalPages = result.totalPages,
                records = result.records.map { BroadcastInboxVO.of(it.broadcast, it.read) },
            )
        )
    }

    @Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_MSG_BROADCAST_READS)
    @PostMapping("/mark-read")
    suspend fun markRead(
        userAuthentication: UserAuthentication,
        @RequestParam broadcastId: Long,
    ): ApiResponse<*> {
        broadcastService.markRead(broadcastId, userAuthentication.userId, userAuthentication.tenantId)
        return ApiResponse.success(null)
    }
}
