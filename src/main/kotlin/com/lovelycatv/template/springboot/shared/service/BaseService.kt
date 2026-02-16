package com.lovelycatv.template.springboot.shared.service

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface BaseService<REPOSITORY: ReactiveCrudRepository<ENTITY, ID>, ENTITY: Any, ID: Any> {
    fun getRepository(): REPOSITORY
}