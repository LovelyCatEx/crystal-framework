package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.types.FileResourceStatus
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface FileResourceRepository : BaseRepository<FileResourceEntity> {
    fun findByMd5(md5: String): Mono<FileResourceEntity>

    fun findByMd5AndScopeAndScopeIdAndStatus(
        md5: String,
        scope: Int,
        scopeId: Long,
        status: Int,
    ): Mono<FileResourceEntity>

    @Modifying
    @Query("UPDATE file_resources SET status = :committedStatus, lease_until = NULL WHERE id = :id AND upload_token = :uploadToken AND status = :uploadingStatus")
    fun commitUpload(id: Long, uploadToken: String, uploadingStatus: Int, committedStatus: Int): Mono<Long>

    @Query("SELECT * FROM file_resources WHERE (status = :uploadingStatus AND lease_until < :now) OR status = :cleanupPendingStatus")
    fun findCleanupCandidates(now: Long, uploadingStatus: Int, cleanupPendingStatus: Int): Flux<FileResourceEntity>

    @Modifying
    @Query("UPDATE file_resources SET status = :cleanupPendingStatus WHERE id = :id AND upload_token = :uploadToken AND status = :uploadingStatus")
    fun markCleanupPending(id: Long, uploadToken: String, uploadingStatus: Int, cleanupPendingStatus: Int): Mono<Long>

    @Modifying
    @Query("DELETE FROM file_resources WHERE id = :id AND upload_token = :uploadToken AND status = :status")
    fun deleteUploadRecord(id: Long, uploadToken: String, status: Int): Mono<Long>

    fun findAllByScopeId(scopeId: Long): Flux<FileResourceEntity>

    @Query("SELECT COUNT(*) FROM file_resources WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
