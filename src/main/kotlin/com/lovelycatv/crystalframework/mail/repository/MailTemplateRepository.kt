package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MailTemplateRepository : BaseRepository<MailTemplateEntity> {
    fun findAllByTypeIdAndActive(typeId: Long, active: Boolean): Flux<MailTemplateEntity>
    fun findByName(name: String): Mono<MailTemplateEntity>

    @Query(
        """
        SELECT * FROM mail_templates 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#typeId == null} = true OR type_id = :typeId)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("typeId") typeId: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<MailTemplateEntity>

    @Query(
        """
        SELECT COUNT(*) FROM mail_templates 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#typeId == null} = true OR type_id = :typeId)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("typeId") typeId: Long?
    ): Mono<Long>
}