package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface FileResourceRepository : BaseRepository<FileResourceEntity> {
    fun findByMd5(md5: String): Mono<FileResourceEntity>

    @Query(
        """
        SELECT * FROM file_resources 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(file_name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(md5) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(object_key) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#type == null} = true OR type = :type)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        keyword: String?,
        type: Int?,
        limit: Int,
        offset: Int
    ): Flux<FileResourceEntity>

    @Query(
        """
        SELECT COUNT(*) FROM file_resources 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(file_name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(md5) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(object_key) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#type == null} = true OR type = :type)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        type: Int?,
    ): Mono<Long>

    @Query("SELECT COUNT(*) FROM file_resources WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}