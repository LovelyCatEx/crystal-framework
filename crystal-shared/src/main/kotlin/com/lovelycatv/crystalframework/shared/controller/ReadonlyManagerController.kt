package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity

/**
 * A read-only variant of [StandardManagerController]. Inherits the `readAll` and `read` endpoints
 * unchanged; the three write endpoints are blocked by [Mutability.READ_ONLY], which throws
 * [com.lovelycatv.crystalframework.shared.exception.ForbiddenException] with the same message as
 * the historical hand-rolled overrides.
 *
 * Use this for resources that are system-generated and should not be mutated through the manager
 * API (e.g. audit logs, mail send logs, user login logs).
 */
abstract class ReadonlyManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    managerService: SERVICE,
) : StandardManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    mutability = Mutability.READ_ONLY,
)
