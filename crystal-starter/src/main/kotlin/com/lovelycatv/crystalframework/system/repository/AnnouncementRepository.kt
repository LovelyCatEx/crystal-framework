package com.lovelycatv.crystalframework.system.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AnnouncementRepository : BaseRepository<AnnouncementEntity> {

    @Query("""
        SELECT * FROM system_announcements
        WHERE status = 1
          AND deleted_time IS NULL
        ORDER BY priority DESC, created_time DESC
    """)
    fun findPublished(): Flux<AnnouncementEntity>
}
