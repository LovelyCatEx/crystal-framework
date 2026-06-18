package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * A read-only variant of [StandardScopedManagerController].
 *
 * Inherits the `readAll` and `query` endpoints from [StandardScopedManagerController],
 * but overrides `create`, `update`, and `delete` to always return 403 Forbidden.
 *
 * Use this for scope-aware resources that are system-generated and should not be mutated
 * through the manager API (e.g. approval flow instances).
 */
@Validated
abstract class ReadonlyScopedManagerController<
        SERVICE : BaseScopedManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseScopedEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadScopedDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    managerService: SERVICE,
    permissions: ScopedPermissionTriad? = null,
) : StandardScopedManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    permissions,
) {
    override suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: CREATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be created")
    }

    override suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: UPDATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be updated")
    }

    override suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: DELETE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be deleted")
    }
}
