package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MailSendLogRepository : BaseRepository<MailSendLogEntity> {
    @Query("SELECT COUNT(*) FROM mail_send_logs WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
