package com.lovelycatv.crystalframework.message.controller

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.sdk.message.config.UserTenantProvider
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * End-user broadcast inbox (read-diffusion): lists the broadcasts a user has not yet
 * read — audience membership is resolved live against the user's tenants — and records
 * a lazy read marker on open. Every endpoint requires an authenticated user.
 */
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/broadcast")
class BroadcastController(
    private val broadcastService: BroadcastService,
    private val userTenantProvider: UserTenantProvider,
) {
    @GetMapping("/unread")
    suspend fun unread(userAuthentication: UserAuthentication): ApiResponse<*> {
        val tenantIds = userTenantProvider.tenantIdsOf(userAuthentication.userId)
        return ApiResponse.success(broadcastService.listUnread(userAuthentication.userId, tenantIds))
    }

    @GetMapping("/unread-count")
    suspend fun unreadCount(userAuthentication: UserAuthentication): ApiResponse<*> {
        val tenantIds = userTenantProvider.tenantIdsOf(userAuthentication.userId)
        return ApiResponse.success(broadcastService.unreadCount(userAuthentication.userId, tenantIds))
    }

    @Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_MSG_BROADCAST_READS)
    @PostMapping("/mark-read")
    suspend fun markRead(
        userAuthentication: UserAuthentication,
        @RequestParam broadcastId: Long,
    ): ApiResponse<*> {
        broadcastService.markRead(broadcastId, userAuthentication.userId)
        return ApiResponse.success(null)
    }
}
