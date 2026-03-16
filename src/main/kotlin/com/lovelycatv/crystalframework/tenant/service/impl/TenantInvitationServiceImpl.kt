package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.tenant.constants.TenantMailDeclaration
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.service.*
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.DepartmentMemberRoleType
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantInvitationServiceImpl(
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userService: UserService,
    private val tenantService: TenantService,
    private val tenantMemberService: TenantMemberService,
    private val tenantMemberManagerService: TenantMemberManagerService,
    private val tenantDepartmentService: TenantDepartmentService,
    private val tenantDepartmentMemberManagerService: TenantDepartmentMemberManagerService,
    private val tenantInvitationRecordService: TenantInvitationRecordService,
    private val mailService: MailService,
) : TenantInvitationService {
    private val logger = logger()

    override val cacheStore: ExpiringKVStore<String, TenantInvitationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantInvitationEntity>>
        get() = redisService.asKVStore()
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
        val memberEntity = tenantMemberService.getByTenantIdAndUserId(tenant.id, userId).also {
            // 1.1 User is already joined this tenant
            logger.info("User ${user.username} - ${user.id} is already in the tenant ${tenant.name} - ${tenant.id}")
        }
            // 1.2 Not found
            ?: tenantMemberManagerService.create(
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

        // 4. Send email to all members authorized to receive review notifications
        if (invitation.requiresReviewing) {
            val tenantMembersToReceiveEmail = tenantService
                .getMembersHasAnyPermission(
                    tenant.id,
                    TenantPermission.ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM
                )
                .mapNotNull {
                    tenantMemberService.getByIdOrNull(it.id)?.let {
                        tenantMemberService.transformTenantMemberVO(it)
                    }
                }
                .filter { it.user != null }

            if (tenantMembersToReceiveEmail.isEmpty()) {
                throw BusinessException("your request is denied as there are no one could handle your request")
            }

            tenantMembersToReceiveEmail.forEach {
                logger.info("sending tenant member join review email of tenant ${tenant.name} to user ${it.user!!.nickname}@${it.user.username}")
                mailService.sendMailByType(
                    to = it.user.email
                        ?: throw BusinessException("request could not be processed as your email is absent"),
                    templateTypeName = TenantMailDeclaration.tenantMemberJoinReviewTemplateType.name,
                    placeholders = mapOf(
                        TenantMailDeclaration.VARIABLE_USERNAME to user.username,
                        TenantMailDeclaration.VARIABLE_NICKNAME to user.nickname,
                        TenantMailDeclaration.VARIABLE_REAL_NAME to realName,
                        TenantMailDeclaration.VARIABLE_PHONE_NUMBER to phoneNumber
                    )
                )
            }
        }
    }
}