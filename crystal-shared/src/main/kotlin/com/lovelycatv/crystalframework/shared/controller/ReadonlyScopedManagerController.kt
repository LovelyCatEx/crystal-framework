package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.service.ScopedRelationshipCheckService
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity

/**
 * A read-only variant of [StandardScopedManagerController]. Inherits the `readAll` and `read`
 * endpoints unchanged; the three write endpoints are blocked by [Mutability.READ_ONLY], which
 * throws [com.lovelycatv.crystalframework.shared.exception.ForbiddenException] with the same
 * message as the historical hand-rolled overrides.
 *
 * Use this for scope-aware resources that are system-generated and should not be mutated through
 * the manager API (e.g. approval flow instances / tasks).
 */
abstract class ReadonlyScopedManagerController<
        SERVICE,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    managerService: SERVICE,
    permissions: PermissionMatrix? = null,
) : StandardScopedManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    permissions,
    mutability = Mutability.READ_ONLY,
) where SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        SERVICE : ScopedRelationshipCheckService
