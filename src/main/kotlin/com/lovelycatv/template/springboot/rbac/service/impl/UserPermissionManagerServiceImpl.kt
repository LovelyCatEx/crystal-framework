package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.request.PaginatedResponseData
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.template.springboot.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserPermissionManagerServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator
) : UserPermissionManagerService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override suspend fun query(baseManagerReadDTO: BaseManagerReadDTO): PaginatedResponseData<UserPermissionEntity> {
        return if (baseManagerReadDTO.id != null) {
            val e = this.getByIdOrNull(baseManagerReadDTO.id!!)
            PaginatedResponseData(
                page = baseManagerReadDTO.page,
                pageSize = baseManagerReadDTO.pageSize,
                total = if (e != null) 1 else 0,
                totalPages = if (e != null) 1 else 0,
                records = if (e != null) listOf(e) else emptyList()
            )
        } else {
            val limit = baseManagerReadDTO.pageSize
            val offset = (baseManagerReadDTO.page - 1) * baseManagerReadDTO.pageSize

            if (baseManagerReadDTO.searchKeyword != null) {
                val keyword = baseManagerReadDTO.searchKeyword!!

                val total = this.getRepository().countByKeyword(keyword).awaitFirstOrNull() ?: 0
                val records = this.getRepository().searchByKeyword(
                    keyword,
                    limit,
                    offset
                ).awaitListWithTimeout()

                baseManagerReadDTO.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            } else {
                val total = this.getRepository().count().awaitFirstOrNull() ?: 0
                val records = this.getRepository()
                    .findAllByPage(limit, offset)
                    .awaitListWithTimeout()

                baseManagerReadDTO.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        }
    }

    override suspend fun create(dto: ManagerCreatePermissionDTO): UserPermissionEntity {
        return this.getRepository().save(
            UserPermissionEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description,
                type = dto.type,
                path = dto.path
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create user permission")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdatePermissionDTO,
        original: UserPermissionEntity
    ): UserPermissionEntity {
        return original.apply {
            if (dto.name != null) {
                this.name = dto.name
            }

            this.description = dto.description

            if (dto.type != null) {
                this.type = dto.type
            }
        }
    }
}