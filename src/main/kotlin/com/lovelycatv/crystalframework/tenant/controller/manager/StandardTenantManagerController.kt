package com.lovelycatv.crystalframework.tenant.controller.manager

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.service.manager.BaseTenantResourceManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Validated
abstract class StandardTenantManagerController<
        SERVICE : BaseTenantResourceManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: BaseManagerCreateTenantResourceDTO,
        READ_DTO: BaseManagerReadTenantResourceDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE,
    protected val createPermission: String,
    protected val scopedCreatePermission: String,
    protected val readPermission: String,
    protected val scopedReadPermission: String,
    protected val updatePermission: String,
    protected val scopedUpdatePermission: String,
    protected val deletePermission: String,
    protected val scopedDeletePermission: String
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam
        tenantId: Long,
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(this.readPermission)) {
            return ApiResponse.success(managerService.findAllByTenantId(tenantId))
        } else if (RbacUtils.hasAuthority(this.scopedReadPermission)) {
            if (tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(managerService.findAllByTenantId(tenantId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: CREATE_DTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(this.createPermission)) {
            managerService.create(dto)
        } else if (RbacUtils.hasAuthority(this.scopedCreatePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (dto.tenantId == userAuthentication.tenantId) {
                managerService.create(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: READ_DTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(this.readPermission)) {
            return ApiResponse.success(managerService.query(dto))
        } else if (RbacUtils.hasAuthority(this.scopedReadPermission)) {
            if (dto.tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(managerService.query(dto))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UPDATE_DTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(this.updatePermission)) {
            managerService.update(dto)
        } else if (RbacUtils.hasAuthority(this.scopedUpdatePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (managerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                managerService.update(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: DELETE_DTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(this.deletePermission)) {
            managerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(this.scopedDeletePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (managerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                managerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }
}