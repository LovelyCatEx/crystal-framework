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
import com.lovelycatv.crystalframework.messagechannel.types.chain.dsl.messageChain
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.utils.SystemChannelConfigProvider
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantMemberRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMessageChannelManagerService
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApprovalCcNotifierImpl(
    private val messageChannelService: MessageChannelService,
    private val systemChannelConfigProvider: SystemChannelConfigProvider,
    private val tenantMessageChannelManagerService: TenantMessageChannelManagerService,
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

        val recipientUserIds = resolveRecipientUserIds(scope, config)
        val emails = recipientUserIds
            .mapNotNull { userService.getByIdOrNull(it)?.email }
            .filter { it.isNotBlank() }
            .toSet()
        if (emails.isEmpty()) {
            logger.warn("Skip CC on node ${node.nodeKey}: no resolvable email recipients")
            return
        }

        val message = buildMessage(instance, node)

        config.channelIds.forEach { channelIdStr ->
            val channelConfig = resolveChannelConfig(scope, channelIdStr) ?: return@forEach
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

    private suspend fun resolveRecipientUserIds(scope: ApprovalFlowScope, config: CcNodeConfig): Set<Long> {
        val directIds = config.userIds.mapNotNull { it.toLongOrNull() }
        val roleIds = config.roleIds.mapNotNull { it.toLongOrNull() }

        return when (scope) {
            ApprovalFlowScope.TENANT -> {
                val memberUserIdsFromDirect = directIds.mapNotNull { memberId ->
                    tenantMemberService.getByIdOrNull(memberId)?.memberUserId
                }
                val memberUserIdsFromRoles = roleIds.flatMap { roleId ->
                    tenantMemberRoleRelationRepository.findAllByRoleId(roleId).asFlow().toList()
                }.mapNotNull { rel ->
                    tenantMemberService.getByIdOrNull(rel.memberId)?.memberUserId
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

    private suspend fun resolveChannelConfig(scope: ApprovalFlowScope, channelIdStr: String): ChannelConfig? {
        return when (scope) {
            ApprovalFlowScope.SYSTEM -> {
                val channelType = runCatching { ChannelType.valueOf(channelIdStr) }.getOrNull()
                if (channelType == null) {
                    logger.warn("CC: unknown system channel id '$channelIdStr'")
                    return null
                }
                systemChannelConfigProvider.resolveOrNull(channelType) ?: run {
                    logger.warn("CC: system channel $channelType is not configured")
                    null
                }
            }
            ApprovalFlowScope.TENANT -> {
                val channelId = channelIdStr.toLongOrNull() ?: run {
                    logger.warn("CC: tenant channel id '$channelIdStr' is not a valid Long")
                    return null
                }
                try {
                    tenantMessageChannelManagerService.resolveConfig(channelId)
                } catch (e: Exception) {
                    logger.warn("CC: failed to resolve tenant channel $channelId: ${e.message}")
                    null
                }
            }
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
