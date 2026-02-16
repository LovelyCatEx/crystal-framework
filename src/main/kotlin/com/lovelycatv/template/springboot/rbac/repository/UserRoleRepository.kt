package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository : ReactiveCrudRepository<UserRoleEntity, Long> {

}