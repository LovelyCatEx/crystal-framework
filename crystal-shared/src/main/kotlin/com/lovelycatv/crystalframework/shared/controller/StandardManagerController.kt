package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.web.bind.annotation.GetMapping

/**
 * Standard manager controller with the four CRUD endpoints inherited from
 * [AbstractManagerController] and a `GET /list` endpoint that returns every row.
 *
 * Authorisation is externalised to `ManagerControllerPermissionAspect` driven by the
 * `@ManagerPermissions` annotation on the concrete subclass; [authorize] therefore stays a no-op.
 */
abstract class StandardManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    managerService: SERVICE,
    mutability: Mutability = Mutability.READ_WRITE,
) : AbstractManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    mutability,
) {
    @GetMapping("/list", version = "1")
    open suspend fun readAll(
        userAuthentication: UserAuthentication,
    ): ApiResponse<*> {
        return ApiResponse.success(managerService.getRepository().findAll().awaitListWithTimeout())
    }
}
