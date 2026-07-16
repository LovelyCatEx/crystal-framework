package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.mail.service.MailTemplateService
import com.lovelycatv.crystalframework.mail.utils.resolveMailTemplatePlaceholders
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.chain.dsl.messageChain
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.utils.SystemChannelConfigProvider
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.tenant.constants.TenantMailDeclaration
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.service.*
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.messagechannel.service.manager.MessageChannelManagerService
import com.lovelycatv.crystalframework.tenant.settings.service.TenantSettingsService
import com.lovelycatv.crystalframework.shared.types.tenant.DepartmentMemberRoleType
import com.lovelycatv.crystalframework.shared.types.tenant.TenantMemberStatus
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantInvitationServiceImpl(
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userService: UserService,
    private val tenantService: TenantService,
    private val tenantMemberService: TenantMemberService,
    private val tenantMemberManagerService: TenantMemberManagerService,
    private val tenantDepartmentService: TenantDepartmentService,
    private val tenantDepartmentMemberManagerService: TenantDepartmentMemberManagerService,
    private val tenantInvitationRecordService: TenantInvitationRecordService,
    private val tenantMemberProfileService: TenantMemberProfileService,
    private val mailTemplateService: MailTemplateService,
    private val messageChannelService: MessageChannelService,
    private val systemChannelConfigProvider: SystemChannelConfigProvider,
    private val tenantMessageChannelManagerService: MessageChannelManagerService,
    private val tenantSettingsService: TenantSettingsService,
) : TenantInvitationService {
    private val logger = logger()

    override val cacheStore: ReactiveExpiringKVStore<String, TenantInvitationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantInvitationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantInvitationEntity> = TenantInvitationEntity::class

    override fun getRepository(): TenantInvitationRepository {
        return this.tenantInvitationRepository
    }

    override suspend fun getInvitationByCode(code: String): TenantInvitationEntity {
        return this.getRepository()
            .getByInvitationCode(code)
            .awaitFirstOrNull()
            ?: throw BusinessException("invitation not found")
    }

    override suspend fun isOverInvitationCount(invitation: TenantInvitationEntity): Boolean {
        val count = tenantInvitationRecordService
            .getRepository()
            .countByInvitationId(invitation.id)
            .awaitFirstOrNull()
            ?: throw BusinessException("could not validate invitation")

        return count >= invitation.invitationCount
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun acceptInvitation(userId: Long, invitationCode: String, realName: String, phoneNumber: String) {
        val invitation = this.getInvitationByCode(invitationCode)

        // 0. Check the invitation code usages
        if (this.isOverInvitationCount(invitation)) {
            throw BusinessException("invitation has reached its usage limit")
        }

        val user = userService.getByIdOrThrow(userId)
        val tenant = tenantService.getByIdOrThrow(invitation.tenantId)

        // 1. Check whether the user is already in the target tenant
        val existingMember = tenantMemberService.getByTenantIdAndUserId(tenant.id, userId)
        if (existingMember != null) {
            // 1.1 User is already joined this tenant
            logger.info("User ${user.username} - ${user.id} is already in the tenant ${tenant.name} - ${tenant.id}")
            throw BusinessException("user is already a member of this tenant")
        }
        // 1.2 Not found — create
        val memberEntity = tenantMemberManagerService.create(
            ManagerCreateTenantMemberDTO(
                tenantId = tenant.id,
                memberUserId = userId,
                status = if (invitation.requiresReviewing)
                    TenantMemberStatus.REVIEWING.typeId
                else
                    TenantMemberStatus.ACTIVE.typeId
            )
        ).also {
            logger.info("User ${user.username} - ${user.id} joined the tenant ${tenant.name} - ${tenant.id}")
        }

        // 1.3 Persist tenant-scoped profile in the same transaction so member and profile stay consistent.
        // Empty / fallback fields stay NULL — UI falls back to system-level users.* on display.
        tenantMemberProfileService.upsertProfile(
            tenantId = tenant.id,
            tenantMemberId = memberEntity.id,
            memberUserId = userId,
            name = realName,
            phone = phoneNumber,
        )

        // 2. Check whether the invitation includes department
        invitation.departmentId?.let { departmentId ->
            // 2.1 Check whether the department exists
            val department = tenantDepartmentService.getByIdOrThrow(departmentId)

            // 2.2 Join department (including check whether the member is already in department)
            tenantDepartmentMemberManagerService.create(
                ManagerCreateTenantDepartmentMemberDTO(
                    departmentId = department.id,
                    memberId = memberEntity.id,
                    roleType = DepartmentMemberRoleType.MEMBER.typeId
                )
            )

            logger.info("User ${user.username} - ${user.id} " +
                    "joined the department ${department.name} - ${department.id} " +
                    "of tenant ${tenant.name} - ${tenant.id}"
            )
        }

        // 3. Record invitation code usage
        tenantInvitationRecordService.saveRecord(invitation.id, userId, realName, phoneNumber)

        // 4. Notify members according to tenant settings
        val tenantSettings = tenantSettingsService.getTenantSettings(tenant.id)
        val notification = tenantSettings.notification

        if (invitation.requiresReviewing) {
            // Resolving reviewers also guards the join request: reviewing is required but
            // nobody can handle it -> deny.
            val reviewerEmails = resolveReviewerEmails(tenant)
            if (notification.memberJoinReview.email) {
                sendMemberJoinReviewEmail(tenant, user, realName, phoneNumber, reviewerEmails)
            } else {
                logger.info("notification.memberJoinReview.email is disabled for tenant ${tenant.name} - ${tenant.id}, skip sending review email")
            }
            notifyViaChannels(
                tenantId = tenant.id,
                channelIds = notification.memberJoinReview.channels,
                content = notification.memberJoinReview.content,
                recipientEmails = reviewerEmails,
                label = "tenant member join review",
            )
        }

        val ownerEmail = resolveOwnerEmail(tenant)
        if (notification.memberJoin.email) {
            sendMemberJoinNotifyEmail(tenant, user, realName, phoneNumber, ownerEmail)
        }
        notifyViaChannels(
            tenantId = tenant.id,
            channelIds = notification.memberJoin.channels,
            content = notification.memberJoin.content,
            recipientEmails = listOfNotNull(ownerEmail),
            label = "tenant member join notify",
        )
    }

    /**
     * Members allowed to review join requests, addressed by email. Throws when reviewing is
     * required but no member can handle it, so the join request is denied early.
     */
    private suspend fun resolveReviewerEmails(tenant: TenantEntity): List<String> {
        val reviewers = tenantService
            .getMembersHasAnyPermission(
                tenant.id,
                TenantPermission.ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM
            )
            .mapNotNull {
                tenantMemberService.getByIdOrNull(it.id)?.let { member ->
                    tenantMemberService.transformTenantMemberVO(member)
                }
            }
            .filter { it.user != null }

        if (reviewers.isEmpty()) {
            throw BusinessException("your request is denied as there are no one could handle your request")
        }

        return reviewers.mapNotNull { it.user?.email }
    }

    private suspend fun resolveOwnerEmail(tenant: TenantEntity): String? {
        val ownerEmail = userService.getByIdOrNull(tenant.ownerUserId)?.email
        if (ownerEmail.isNullOrBlank()) {
            logger.info("Owner of tenant ${tenant.name} - ${tenant.id} has no email")
            return null
        }
        return ownerEmail
    }

    private suspend fun sendMemberJoinReviewEmail(
        tenant: TenantEntity,
        user: UserEntity,
        realName: String,
        phoneNumber: String,
        reviewerEmails: List<String>,
    ) {
        val placeholders = mapOf(
            TenantMailDeclaration.VARIABLE_USERNAME to user.username,
            TenantMailDeclaration.VARIABLE_NICKNAME to user.nickname,
            TenantMailDeclaration.VARIABLE_REAL_NAME to realName,
            TenantMailDeclaration.VARIABLE_PHONE_NUMBER to phoneNumber,
        )
        val message = buildMailMessage(TenantMailDeclaration.tenantMemberJoinReviewTemplateType.name, placeholders)

        reviewerEmails.forEach { email ->
            logger.info("sending tenant member join review email of tenant ${tenant.name} to $email")
            sendEmailOrThrow(email, message, "tenant member join review email")
        }
    }

    private suspend fun sendMemberJoinNotifyEmail(
        tenant: TenantEntity,
        user: UserEntity,
        realName: String,
        phoneNumber: String,
        ownerEmail: String?,
    ) {
        if (ownerEmail.isNullOrBlank()) {
            logger.info("Skip sending member join notify email: owner of tenant ${tenant.name} - ${tenant.id} has no email")
            return
        }

        val placeholders = mapOf(
            TenantMailDeclaration.VARIABLE_USERNAME to user.username,
            TenantMailDeclaration.VARIABLE_NICKNAME to user.nickname,
            TenantMailDeclaration.VARIABLE_REAL_NAME to realName,
            TenantMailDeclaration.VARIABLE_PHONE_NUMBER to phoneNumber,
            TenantMailDeclaration.VARIABLE_TENANT_NAME to tenant.name,
        )
        val message = buildMailMessage(TenantMailDeclaration.tenantMemberJoinNotifyTemplateType.name, placeholders)

        logger.info("sending tenant member join notify email of tenant ${tenant.name} to owner $ownerEmail")
        sendEmailOrThrow(ownerEmail, message, "tenant member join notify email")
    }

    /**
     * Sends [content] (a [MessageChain] XML body) to every recipient over each selected tenant
     * message channel. Best-effort: failures are logged and never abort the join flow.
     */
    private suspend fun notifyViaChannels(
        tenantId: Long,
        channelIds: List<Long>,
        content: String,
        recipientEmails: List<String>,
        label: String,
    ) {
        if (channelIds.isEmpty() || content.isBlank() || recipientEmails.isEmpty()) {
            return
        }

        val message = ChainMessage(chain = MessageChain.parse(content))

        channelIds.forEach { channelId ->
            val config = try {
                tenantMessageChannelManagerService.resolveConfig(channelId)
            } catch (e: Exception) {
                logger.warn("Skip channel $channelId for $label of tenant $tenantId: ${e.message}")
                return@forEach
            }

            recipientEmails.forEach { email ->
                val recipient = buildRecipient(config.channelType, email)
                val result = messageChannelService.send(config, recipient, message)
                if (!result.success) {
                    logger.error(
                        "Failed to send {} to {} via channel {} ({}): [{}] {}",
                        label, email, channelId, result.channelType, result.errorCode, result.errorMessage,
                    )
                }
            }
        }
    }

    private fun buildRecipient(channelType: ChannelType, email: String): MessageRecipient = when (channelType) {
        ChannelType.EMAIL -> EmailRecipient(email = email)
        ChannelType.LARK -> LarkRecipient(email = email)
    }

    private suspend fun buildMailMessage(templateTypeName: String, placeholders: Map<String, String>): ChainMessage {
        val template = mailTemplateService.getAvailableTemplateByTypeName(templateTypeName)
        return ChainMessage(
            title = template.title.resolveMailTemplatePlaceholders(placeholders),
            chain = messageChain {
                rawHtml(template.content.resolveMailTemplatePlaceholders(placeholders))
            },
        )
    }

    private suspend fun sendEmailOrThrow(email: String, message: ChainMessage, label: String) {
        val config = systemChannelConfigProvider.resolve(ChannelType.EMAIL)
        val result = messageChannelService.send(config, EmailRecipient(email = email), message)
        if (!result.success) {
            logger.error(
                "Failed to send {} to {} via {}: [{}] {}",
                label, email, result.channelType, result.errorCode, result.errorMessage,
            )
            throw BusinessException("Send email to $email failed")
        }
    }
}