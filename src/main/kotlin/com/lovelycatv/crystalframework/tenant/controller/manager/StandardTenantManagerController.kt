package com.lovelycatv.crystalframework.tenant.controller.manager

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.entity.ScopedEntity
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

/**
 * Base controller for tenant-scoped manager endpoints (create / query / list / update / delete).
 *
 * The class wires up the standard authorization flow that every CRUD operation needs:
 *
 *  1. Holders of the **system** permission can act unconditionally.
 *  2. Holders of the **scoped** permission can only act within their own tenant — the
 *     check is delegated to the overridable `isXxxInScope` hooks below.
 *  3. Otherwise the request is rejected.
 *
 * The defaults assume the resource is *directly* tenant-scoped (i.e. its DTO carries a
 * `tenantId`). Resources nested deeper in the tenant hierarchy (for example a
 * department member, whose `departmentId` is the immediate parent and whose tenant
 * must be looked up via the parent service) should override the relevant scope-check
 * hook to plug in their own logic — typically by delegating to
 * [BaseTenantResourceManagerService.checkIsRelatedToRootParent] on a sibling service.
 *
 * `customXxx` hooks are also still available for cases where the entire flow needs to
 * be replaced (return a non-null [ApiResponse] to short-circuit).
 */
@Validated
abstract class StandardTenantManagerController<
        SERVICE : BaseTenantResourceManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE,
    protected val createPermission: String,
    /**
     * Tenant-scoped permission required to perform the action against your own tenant.
     * Pass [DISABLED_SCOPED_PERMISSION] (or any blank string) to disable scoped access entirely;
     * in that case only holders of the corresponding system permission can call the endpoint.
     */
    protected val scopedCreatePermission: String,
    protected val readPermission: String,
    protected val scopedReadPermission: String,
    protected val updatePermission: String,
    protected val scopedUpdatePermission: String,
    protected val deletePermission: String,
    protected val scopedDeletePermission: String
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long> {
    companion object {
        /** Sentinel for [scopedCreatePermission] etc. meaning "tenant-scoped users are not allowed". */
        const val DISABLED_SCOPED_PERMISSION: String = ""

        private const val DEFAULT_SCOPE_CHECK_REQUIRES_TENANT_DTO =
            "Default scope check expects the DTO to extend %s. Override the corresponding " +
                    "isXxxInScope() in your controller to provide a custom scope check."
    }

    private suspend fun hasScopedAuthority(authority: String): Boolean {
        if (authority.isBlank()) return false
        return RbacUtils.hasAuthority(authority)
    }

    // region Scope-check hooks (override to integrate non-direct-tenant resources)

    /**
     * Whether the create [dto] targets data that lives within the calling user's tenant.
     * Default: cast to [BaseManagerCreateTenantResourceDTO] and compare `tenantId`.
     */
    protected suspend fun isCreateInScope(
        dto: CREATE_DTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        val tenantDto = dto as? BaseManagerCreateTenantResourceDTO
            ?: error(DEFAULT_SCOPE_CHECK_REQUIRES_TENANT_DTO.format(BaseManagerCreateTenantResourceDTO::class.simpleName))
        return tenantDto.tenantId == userAuthentication.tenantId
    }

    /**
     * Whether the read [dto] targets data that lives within the calling user's tenant.
     * Default: cast to [BaseManagerReadTenantResourceDTO] and compare `tenantId`.
     */
    protected suspend fun isQueryInScope(
        dto: READ_DTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        val tenantDto = dto as? BaseManagerReadTenantResourceDTO
            ?: error(DEFAULT_SCOPE_CHECK_REQUIRES_TENANT_DTO.format(BaseManagerReadTenantResourceDTO::class.simpleName))
        return tenantDto.tenantId == userAuthentication.tenantId
    }

    /**
     * Whether the [tenantId] passed to the list endpoint matches the calling user's tenant.
     * Default: simple equality.
     */
    protected suspend fun isReadAllInScope(
        tenantId: Long,
        userAuthentication: UserAuthentication
    ): Boolean {
        return tenantId == userAuthentication.tenantId
    }

    /**
     * Whether the update [dto] targets an entity that ultimately belongs to the calling user's
     * tenant. Default: walk the parent chain via
     * [BaseTenantResourceManagerService.checkIsRelatedToRootParent].
     */
    protected suspend fun isUpdateInScope(
        dto: UPDATE_DTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return managerService.checkIsRelatedToRootParent(dto.id, userAuthentication.tenantId!!)
    }

    /**
     * Whether the delete [dto] targets entities that ultimately belong to the calling user's
     * tenant. Default: walk the parent chain via
     * [BaseTenantResourceManagerService.checkIsRelatedToRootParent].
     */
    protected suspend fun isDeleteInScope(
        dto: DELETE_DTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return managerService.checkIsRelatedToRootParent(dto.ids, userAuthentication.tenantId!!)
    }

    // endregion

    // region Response shaping hooks (override to return VOs instead of raw entities)

    /** Shape the response body for [query]. Default returns paginated entities. */
    protected suspend fun buildQueryResponse(dto: READ_DTO): Any {
        return managerService.query(dto)
    }

    /** Shape the response body for [readAll]. Default returns the entities of the tenant. */
    protected suspend fun buildReadAllResponse(tenantId: Long): Any {
        return managerService.findAllByTenantId(tenantId)
    }

    // endregion

    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam
        tenantId: Long,
    ): ApiResponse<*> {
        customReadAll(userAuthentication, tenantId)?.let { return it }

        return if (RbacUtils.hasAuthority(this.readPermission)) {
            ApiResponse.success(buildReadAllResponse(tenantId))
        } else if (hasScopedAuthority(this.scopedReadPermission)) {
            if (isReadAllInScope(tenantId, userAuthentication)) {
                ApiResponse.success(buildReadAllResponse(tenantId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    /**
     * Hook for [readAll]. Return non-null to short-circuit the standard logic with a custom response;
     * return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customReadAll(
        userAuthentication: UserAuthentication,
        tenantId: Long,
    ): ApiResponse<*>? = null

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: CREATE_DTO
    ): ApiResponse<*> {
        customCreate(userAuthentication, dto)?.let { return it }

        if (RbacUtils.hasAuthority(this.createPermission)) {
            managerService.create(dto)
        } else if (hasScopedAuthority(this.scopedCreatePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (isCreateInScope(dto, userAuthentication)) {
                managerService.create(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    /**
     * Hook for [create]. Return non-null to short-circuit the standard logic with a custom response;
     * return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: CREATE_DTO
    ): ApiResponse<*>? = null

    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: READ_DTO
    ): ApiResponse<*> {
        customQuery(userAuthentication, dto)?.let { return it }

        if (RbacUtils.hasAuthority(this.readPermission)) {
            return ApiResponse.success(buildQueryResponse(dto))
        } else if (hasScopedAuthority(this.scopedReadPermission)) {
            if (isQueryInScope(dto, userAuthentication)) {
                return ApiResponse.success(buildQueryResponse(dto))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    /**
     * Hook for [query]. Return non-null to short-circuit the standard logic with a custom response;
     * return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customQuery(
        userAuthentication: UserAuthentication,
        dto: READ_DTO
    ): ApiResponse<*>? = null

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UPDATE_DTO
    ): ApiResponse<*> {
        customUpdate(userAuthentication, dto)?.let { return it }

        if (RbacUtils.hasAuthority(this.updatePermission)) {
            managerService.update(dto)
        } else if (hasScopedAuthority(this.scopedUpdatePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (isUpdateInScope(dto, userAuthentication)) {
                managerService.update(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    /**
     * Hook for [update]. Return non-null to short-circuit the standard logic with a custom response;
     * return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customUpdate(
        userAuthentication: UserAuthentication,
        dto: UPDATE_DTO
    ): ApiResponse<*>? = null

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: DELETE_DTO
    ): ApiResponse<*> {
        customDelete(userAuthentication, dto)?.let { return it }

        if (RbacUtils.hasAuthority(this.deletePermission)) {
            managerService.deleteByDTO(dto)
        } else if (hasScopedAuthority(this.scopedDeletePermission)) {
            userAuthentication.assertTenantIdNotNull()
            if (isDeleteInScope(dto, userAuthentication)) {
                managerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    /**
     * Hook for [delete]. Return non-null to short-circuit the standard logic with a custom response;
     * return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customDelete(
        userAuthentication: UserAuthentication,
        dto: DELETE_DTO
    ): ApiResponse<*>? = null
}
