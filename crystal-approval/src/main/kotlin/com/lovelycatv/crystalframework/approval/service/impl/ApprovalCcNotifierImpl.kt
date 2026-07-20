package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.constants.ApprovalCcConstants
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.service.ApprovalCcNotifier
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowDefinitionService
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.approval.types.CcNodeConfig
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.service.manager.MessageChannelManagerService
import com.lovelycatv.crystalframework.messagechannel.types.chain.dsl.messageChain
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantMemberRoleRelationRepository
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApprovalCcNotifierImpl(
    private val messageChannelService: MessageChannelService,
    private val messageChannelManagerService: MessageChannelManagerService,
    private val tenantMemberService: TenantMemberService,
    private val tenantMemberRoleRelationRepository: TenantMemberRoleRelationRepository,
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userService: UserService,
    private val definitionService: ApprovalFlowDefinitionService,
) : ApprovalCcNotifier {

    private val logger = logger()

    override suspend fun notify(
        instance: ApprovalFlowInstanceEntity,
        node: ApprovalFlowNodeEntity,
        config: CcNodeConfig,
    ) {
        if (config.channelIds.isEmpty()) return

        val scope = ApprovalFlowScope.getById(instance.scope)
        if (scope == null) {
            logger.warn("Skip CC on node ${node.nodeKey}: unknown flow scope ${instance.scope}")
            return
        }

        val recipientUserIds = resolveRecipientUserIds(scope, config, instance.scopeId)
        val emails = recipientUserIds
            .mapNotNull { userService.getByIdOrNull(it)?.email }
            .filter { it.isNotBlank() }
            .toSet()
        if (emails.isEmpty()) {
            logger.warn("Skip CC on node ${node.nodeKey}: no resolvable email recipients")
            return
        }

        val message = buildMessage(instance, node)

        val resourceScope = when (scope) {
            ApprovalFlowScope.SYSTEM -> ResourceScope.SYSTEM
            ApprovalFlowScope.TENANT -> ResourceScope.TENANT
        }

        config.channelIds.forEach { channelIdStr ->
            val channelConfig = resolveChannelConfig(channelIdStr, resourceScope, instance.scopeId) ?: return@forEach
            val recipients: List<MessageRecipient> = emails.map { buildRecipient(channelConfig.channelType, it) }
            try {
                val results = messageChannelService.broadcast(channelConfig, recipients, message)
                results.forEachIndexed { index, result ->
                    if (!result.success) {
                        val email = recipients[index].let { r ->
                            when (r) {
                                is EmailRecipient -> r.email
                                is LarkRecipient -> r.email ?: "-"
                            }
                        }
                        logger.warn(
                            "CC send failed on node {} channel {} to {}: [{}] {}",
                            node.nodeKey, channelIdStr, email, result.errorCode, result.errorMessage,
                        )
                    }
                }
            } catch (t: Throwable) {
                logger.warn("CC send threw on node ${node.nodeKey} channel $channelIdStr: ${t.message}", t)
            }
        }
    }

    private suspend fun resolveRecipientUserIds(
        scope: ApprovalFlowScope,
        config: CcNodeConfig,
        instanceTenantId: Long,
    ): Set<Long> {
        val directIds = config.userIds.mapNotNull { it.toLongOrNull() }
        val roleIds = config.roleIds.mapNotNull { it.toLongOrNull() }

        return when (scope) {
            ApprovalFlowScope.TENANT -> {
                val memberUserIdsFromDirect = directIds.mapNotNull { memberId ->
                    resolveTenantMemberUserId(memberId, instanceTenantId)
                }
                val memberUserIdsFromRoles = roleIds.flatMap { roleId ->
                    tenantMemberRoleRelationRepository.findAllByRoleId(roleId).asFlow().toList()
                }.mapNotNull { rel ->
                    resolveTenantMemberUserId(rel.memberId, instanceTenantId)
                }
                (memberUserIdsFromDirect + memberUserIdsFromRoles).toSet()
            }
            ApprovalFlowScope.SYSTEM -> {
                val userIdsFromRoles = roleIds.flatMap { roleId ->
                    userRoleRelationRepository.findByRoleId(roleId).asFlow().toList()
                }.map { it.userId }
                (directIds + userIdsFromRoles).toSet()
            }
        }
    }

    /**
     * Look up a tenant member by id and return its userId only when the member belongs to the
     * expected tenant. Cross-tenant references (which could arise from a maliciously crafted CC
     * node config) are dropped with a warn log so the rest of the CC delivery still proceeds.
     */
    private suspend fun resolveTenantMemberUserId(memberId: Long, expectedTenantId: Long): Long? {
        val member = tenantMemberService.getByIdOrNull(memberId) ?: return null
        if (member.tenantId != expectedTenantId) {
            logger.warn(
                "CC recipient tenantMember {} belongs to tenant {} but instance is on tenant {}, skipped",
                memberId, member.tenantId, expectedTenantId,
            )
            return null
        }
        return member.memberUserId
    }

    private suspend fun resolveChannelConfig(
        channelIdStr: String,
        expectedScope: ResourceScope,
        expectedScopeId: Long?,
    ): ChannelConfig? {
        val channelId = channelIdStr.toLongOrNull() ?: run {
            logger.warn("CC: channel id '$channelIdStr' is not a valid Long")
            return null
        }
        return try {
            messageChannelManagerService.resolveConfig(channelId, expectedScope, expectedScopeId)
        } catch (e: Exception) {
            logger.warn("CC: failed to resolve message channel $channelId: ${e.message}")
            null
        }
    }

    private suspend fun buildMessage(
        instance: ApprovalFlowInstanceEntity,
        node: ApprovalFlowNodeEntity,
    ): ChainMessage {
        val definitionName = definitionService.getByIdOrNull(instance.definitionId)?.name ?: "-"
        val initiatedAt = Instant.ofEpochMilli(instance.createdTime).toString()
        return ChainMessage(
            title = ApprovalCcConstants.MESSAGE_TITLE_PREFIX + definitionName,
            chain = messageChain {
                text(ApprovalCcConstants.LINE_FLOW_NAME.format(definitionName)); newline()
                text(ApprovalCcConstants.LINE_NODE_NAME.format(node.name)); newline()
                text(ApprovalCcConstants.LINE_INITIATOR.format(instance.initiatorId.toString())); newline()
                text(ApprovalCcConstants.LINE_INSTANCE_ID.format(instance.id.toString())); newline()
                text(ApprovalCcConstants.LINE_INITIATED_AT.format(initiatedAt))
            },
        )
    }

    private fun buildRecipient(channelType: ChannelType, email: String): MessageRecipient = when (channelType) {
        ChannelType.EMAIL -> EmailRecipient(email = email)
        ChannelType.LARK -> LarkRecipient(email = email)
    }
}
