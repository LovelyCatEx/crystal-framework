package com.lovelycatv.crystalframework.message.controller

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.message.constants.MessageConstants
import com.lovelycatv.crystalframework.message.controller.dto.SendInTenantMessageDTO
import com.lovelycatv.crystalframework.message.controller.dto.SendMessageDTO
import com.lovelycatv.crystalframework.message.controller.dto.SendTenantMessageDTO
import com.lovelycatv.crystalframework.message.controller.dto.SendToMemberDTO
import com.lovelycatv.crystalframework.message.controller.dto.SendToTenantDTO
import com.lovelycatv.crystalframework.message.service.InboxService
import com.lovelycatv.crystalframework.message.service.MessageService
import com.lovelycatv.crystalframework.message.service.MsgConversationMemberService
import com.lovelycatv.crystalframework.message.types.ContentType
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.config.ContactableTenantProvider
import com.lovelycatv.crystalframework.sdk.message.config.UserTenantProvider
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * End-user messaging: point-to-point send plus the unified inbox (write-diffusion
 * conversations). Every endpoint requires an authenticated user; a user may only read
 * conversations they are a member of. Broadcast (read-diffusion) endpoints live in
 * [BroadcastController].
 */
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/message")
class MessageController(
    private val messageService: MessageService,
    private val inboxService: InboxService,
    private val msgConversationMemberService: MsgConversationMemberService,
    private val userTenantProvider: UserTenantProvider,
    private val contactableTenantProvider: ContactableTenantProvider,
) {
    @Audit(action = AuditAction.CREATE, resourceType = TableConstants.TABLE_MSG_MESSAGES)
    @PostMapping("/send")
    suspend fun send(
        userAuthentication: UserAuthentication,
        @RequestBody dto: SendMessageDTO,
    ): ApiResponse<*> {
        val targetUserId = dto.targetUserId.toLongOrNull()
            ?: throw BusinessException("Invalid targetUserId: ${dto.targetUserId}")
        val contentType = dto.contentType?.let {
            ContentType.getByTypeId(it) ?: throw BusinessException("Unknown content type: $it")
        } ?: ContentType.TEXT
        val message = messageService.send(
            scope = Scope(ScopeType.SYSTEM, null),
            sender = Party(PartyType.USER, userAuthentication.userId),
            target = Party(PartyType.USER, targetUserId),
            content = dto.content,
            contentType = contentType,
            actingUserId = userAuthentication.userId,
        )
        return ApiResponse.success(message)
    }

    @Audit(action = AuditAction.CREATE, resourceType = TableConstants.TABLE_MSG_MESSAGES)
    @PostMapping("/send-in-tenant")
    suspend fun sendInTenant(
        userAuthentication: UserAuthentication,
        @RequestBody dto: SendInTenantMessageDTO,
    ): ApiResponse<*> {
        val tenantId = dto.tenantId.toLongOrNull()
            ?: throw BusinessException("Invalid tenantId: ${dto.tenantId}")
        val targetUserId = dto.targetUserId.toLongOrNull()
            ?: throw BusinessException("Invalid targetUserId: ${dto.targetUserId}")
        val contentType = dto.contentType?.let {
            ContentType.getByTypeId(it) ?: throw BusinessException("Unknown content type: $it")
        } ?: ContentType.TEXT
        val message = messageService.send(
            scope = Scope(ScopeType.TENANT, tenantId),
            sender = Party(PartyType.USER, userAuthentication.userId),
            target = Party(PartyType.USER, targetUserId),
            content = dto.content,
            contentType = contentType,
            actingUserId = userAuthentication.userId,
            enforceTargetScopeMembership = true,
        )
        return ApiResponse.success(message)
    }

    @Audit(action = AuditAction.CREATE, resourceType = TableConstants.TABLE_MSG_MESSAGES)
    @PostMapping("/send-as-tenant")
    suspend fun sendAsTenant(
        userAuthentication: UserAuthentication,
        @RequestBody dto: SendTenantMessageDTO,
    ): ApiResponse<*> {
        val tenantId = dto.tenantId.toLongOrNull()
            ?: throw BusinessException("Invalid tenantId: ${dto.tenantId}")
        val targetUserId = dto.targetUserId.toLongOrNull()
            ?: throw BusinessException("Invalid targetUserId: ${dto.targetUserId}")
        val contentType = dto.contentType?.let {
            ContentType.getByTypeId(it) ?: throw BusinessException("Unknown content type: $it")
        } ?: ContentType.TEXT
        val message = messageService.send(
            scope = Scope(ScopeType.TENANT, tenantId),
            sender = Party(PartyType.TENANT, tenantId),
            target = Party(PartyType.USER, targetUserId),
            content = dto.content,
            contentType = contentType,
            actingUserId = userAuthentication.userId,
        )
        return ApiResponse.success(message)
    }

    @Audit(action = AuditAction.CREATE, resourceType = TableConstants.TABLE_MSG_MESSAGES)
    @PostMapping("/send-to-tenant")
    suspend fun sendToTenant(
        userAuthentication: UserAuthentication,
        @RequestBody dto: SendToTenantDTO,
    ): ApiResponse<*> {
        val tenantId = dto.tenantId.toLongOrNull()
            ?: throw BusinessException("Invalid tenantId: ${dto.tenantId}")
        val contentType = dto.contentType?.let {
            ContentType.getByTypeId(it) ?: throw BusinessException("Unknown content type: $it")
        } ?: ContentType.TEXT
        val message = messageService.send(
            scope = Scope(ScopeType.TENANT, tenantId),
            sender = Party(PartyType.USER, userAuthentication.userId),
            target = Party(PartyType.TENANT, tenantId),
            content = dto.content,
            contentType = contentType,
            actingUserId = userAuthentication.userId,
            enforceScopeMembership = false,
        )
        return ApiResponse.success(message)
    }

    @Audit(action = AuditAction.CREATE, resourceType = TableConstants.TABLE_MSG_MESSAGES)
    @PostMapping("/send-to-member")
    suspend fun sendToMember(
        userAuthentication: UserAuthentication,
        @RequestBody dto: SendToMemberDTO,
    ): ApiResponse<*> {
        val tenantId = dto.tenantId.toLongOrNull()
            ?: throw BusinessException("Invalid tenantId: ${dto.tenantId}")
        val targetUserId = dto.targetUserId.toLongOrNull()
            ?: throw BusinessException("Invalid targetUserId: ${dto.targetUserId}")
        val contentType = dto.contentType?.let {
            ContentType.getByTypeId(it) ?: throw BusinessException("Unknown content type: $it")
        } ?: ContentType.TEXT
        val message = messageService.send(
            scope = Scope(ScopeType.TENANT, tenantId),
            sender = Party(PartyType.USER, userAuthentication.userId),
            target = Party(PartyType.USER, targetUserId),
            content = dto.content,
            contentType = contentType,
            actingUserId = userAuthentication.userId,
            enforceScopeMembership = true,
            enforceTargetScopeMembership = false,
        )
        return ApiResponse.success(message)
    }

    @GetMapping("/contactable-tenants")
    suspend fun contactableTenants(
        userAuthentication: UserAuthentication,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ApiResponse<*> {
        val result = contactableTenantProvider.pageContactableTenants(keyword, page, pageSize)
        return ApiResponse.success(result)
    }

    @GetMapping("/inbox-conversations")
    suspend fun inboxConversations(
        userAuthentication: UserAuthentication,
        @RequestParam(required = false) currentTenantId: Long?,
    ): ApiResponse<*> =
        ApiResponse.success(inboxService.listConversations(userAuthentication.userId, currentTenantId))

    @GetMapping("/inbox-unread-count")
    suspend fun inboxUnreadCount(userAuthentication: UserAuthentication): ApiResponse<*> {
        val tenantIds = userTenantProvider.tenantIdsOf(userAuthentication.userId)
        return ApiResponse.success(inboxService.totalUnread(userAuthentication.userId, tenantIds))
    }

    @GetMapping("/conversation-messages")
    suspend fun conversationMessages(
        userAuthentication: UserAuthentication,
        @RequestParam conversationId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ApiResponse<*> {
        assertMember(conversationId, userAuthentication.userId)
        val cappedPageSize = pageSize.coerceIn(1, MessageConstants.MAX_CONVERSATION_PAGE_SIZE)
        return ApiResponse.success(messageService.getConversationMessages(conversationId, page, cappedPageSize))
    }

    @Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_MSG_CONVERSATION_MEMBERS)
    @PostMapping("/mark-conversation-read")
    suspend fun markConversationRead(
        userAuthentication: UserAuthentication,
        @RequestParam conversationId: Long,
    ): ApiResponse<*> {
        assertMember(conversationId, userAuthentication.userId)
        messageService.markConversationRead(conversationId, userAuthentication.userId)
        return ApiResponse.success(null)
    }

    private suspend fun assertMember(conversationId: Long, userId: Long) {
        val isMember = msgConversationMemberService.listByUser(userId).any { it.conversationId == conversationId }
        if (!isMember) {
            throw ForbiddenException(
                "Not a member of conversation $conversationId",
                context = ForbiddenContext(
                    reason = ForbiddenReason.SCOPE_MISMATCH,
                    scope = ResourceScope.SYSTEM,
                ),
            )
        }
    }
}
