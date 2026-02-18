package com.lovelycatv.template.springboot.shared.repository

import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface BaseRepository<ENTITY: BaseEntity> : ReactiveCrudRepository<ENTITY, Long> {
    fun findAllByPageable(pageable: Pageable): Mono<Page<ENTITY>>

    fun searchAllByPageable(keyword: String, pageable: Pageable): Mono<Page<ENTITY>>
}