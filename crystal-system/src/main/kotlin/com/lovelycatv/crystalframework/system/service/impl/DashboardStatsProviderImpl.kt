package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.monitor.service.DashboardStatsProvider
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRecordRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DashboardStatsProviderImpl(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val tenantMemberRepository: TenantMemberRepository,
    private val fileResourceRepository: FileResourceRepository,
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val tenantInvitationRecordRepository: TenantInvitationRecordRepository,
    private val oAuthAccountRepository: OAuthAccountRepository,
    private val mailSendLogRepository: MailSendLogRepository,
) : DashboardStatsProvider {

    override fun countUsers(startTime: Long, endTime: Long): Mono<Long> =
        userRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countTenants(startTime: Long, endTime: Long): Mono<Long> =
        tenantRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countTenantMembers(startTime: Long, endTime: Long): Mono<Long> =
        tenantMemberRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countFileResources(startTime: Long, endTime: Long): Mono<Long> =
        fileResourceRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countTenantInvitations(startTime: Long, endTime: Long): Mono<Long> =
        tenantInvitationRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countTenantInvitationRecords(startTime: Long, endTime: Long): Mono<Long> =
        tenantInvitationRecordRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countOAuthAccounts(startTime: Long, endTime: Long): Mono<Long> =
        oAuthAccountRepository.countByCreatedTimeBetween(startTime, endTime)

    override fun countMailSent(startTime: Long, endTime: Long): Mono<Long> =
        mailSendLogRepository.countByCreatedTimeBetween(startTime, endTime)
}
