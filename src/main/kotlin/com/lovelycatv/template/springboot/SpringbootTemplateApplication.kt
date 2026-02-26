package com.lovelycatv.template.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.constants.SystemRole
import com.lovelycatv.template.springboot.rbac.constants.SystemRolePermissionRelation
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.rbac.service.UserRoleManagerService
import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.rbac.types.PermissionType
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.log.logger
import com.sun.beans.introspect.PropertyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.reflect.full.memberProperties

@SpringBootApplication
class SpringbootTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}