package com.lovelycatv.crystalframework.system.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AnnouncementRepository : BaseRepository<AnnouncementEntity> {

    // status=1 (PUBLISHED), target IN (0=USER_ONLY, 2=BOTH)
    @Query("""
        SELECT * FROM system_announcements
        WHERE status = 1
          AND target IN (0, 2)
          AND deleted_time IS NULL
        ORDER BY priority DESC, created_time DESC
    """)
    fun findPublishedForUser(): Flux<AnnouncementEntity>

    // status=1 (PUBLISHED), target IN (1=MANAGER_ONLY, 2=BOTH)
    @Query("""
        SELECT * FROM system_announcements
        WHERE status = 1
          AND target IN (1, 2)
          AND deleted_time IS NULL
        ORDER BY priority DESC, created_time DESC
    """)
    fun findPublishedForManager(): Flux<AnnouncementEntity>
}
