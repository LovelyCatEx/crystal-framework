package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

/**
 * [com.lovelycatv.crystalframework.shared.aspect.ManagerControllerPermissionAspect] is using the name of methods in this class.
 */
@Validated
abstract class StandardManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(managerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: CREATE_DTO
    ): ApiResponse<*> {
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @GetMapping("/query", version = "1")
    suspend fun read(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: READ_DTO
    ): ApiResponse<*> {
        return ApiResponse.success(managerService.query(dto))
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UPDATE_DTO
    ): ApiResponse<*> {
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: DELETE_DTO
    ): ApiResponse<*> {
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}