package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface FileResourceRepository : BaseRepository<FileResourceEntity> {
    fun findByMd5(md5: String): Mono<FileResourceEntity>

}