package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateTenantResourceDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Base controller for tenant-scoped manager endpoints. Inherits the four DTO-shaped CRUD endpoints
 * from [AbstractManagerController] and adds a `GET /list` endpoint that requires an explicit
 * `tenantId` query parameter.
 *
 * The class wires up the standard authorisation flow that every CRUD operation needs:
 *
 *  1. Holders of the **tenantAdmin** permission (e.g. `tenant.role.create`) can act unconditionally
 *     — the cross-tenant layer needs no `tenantId` match.
 *  2. Holders of the **tenantPem** permission (e.g. `i.tenant.role.create`) can only act within
 *     their own tenant — the check is delegated to the overridable `isXxxInScope` hooks below.
 *  3. Otherwise the request is rejected.
 *
 * Authorisation is driven by a [PermissionMatrix] passed to the constructor; use
 * [PermissionMatrix.tenantOnly] to build one that leaves `super` / `system` layers as
 * [PermissionMatrix.NOT_APPLICABLE] (the common case — Tenant-only resources have no SYSTEM scope
 * semantics).
 *
 * The defaults assume the resource is *directly* tenant-scoped (i.e. its DTO carries a
 * `tenantId`). Resources nested deeper in the tenant hierarchy (for example a department member,
 * whose `departmentId` is the immediate parent and whose tenant must be looked up via the parent
 * service) should override the relevant scope-check hook to plug in their own logic — typically
 * by delegating to
 * [com.lovelycatv.crystalframework.shared.service.TenantRelationshipCheckService.checkIsRelatedToRootParent]
 * on a sibling service.
 *
 * `customXxx` hooks are still available for cases where the entire flow needs to be replaced
 * (return a non-null [ApiResponse] to short-circuit). The endpoint methods on this class are
 * overridden to run the short-circuit check before delegating to the parent's standard path.
 */
abstract class StandardTenantManagerController<
        SERVICE : BaseTenantResourceManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadTenantResourceDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    managerService: SERVICE,
    /**
     * Four-layer permission matrix. For Tenant-only resources use [PermissionMatrix.tenantOnly]
     * which leaves `super` / `system` layers as [PermissionMatrix.NOT_APPLICABLE].
     */
    protected val permissions: PermissionMatrix,
    mutability: Mutability = Mutability.READ_WRITE,
) : AbstractManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    mutability,
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long> {

    companion object {
        private const val DEFAULT_SCOPE_CHECK_REQUIRES_TENANT_DTO =
            "Default scope check expects the DTO to extend %s. Override the corresponding " +
                    "isXxxInScope() in your controller to provide a custom scope check."
    }

    private suspend fun hasScopedAuthority(authority: String): Boolean {
        // NOT_APPLICABLE means the layer does not apply to this resource: short-circuit to false
        // without touching RBAC.
        if (authority == PermissionMatrix.NOT_APPLICABLE) return false
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
     * [com.lovelycatv.crystalframework.shared.service.TenantRelationshipCheckService.checkIsRelatedToRootParent].
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
     * [com.lovelycatv.crystalframework.shared.service.TenantRelationshipCheckService.checkIsRelatedToRootParent].
     */
    protected suspend fun isDeleteInScope(
        dto: DELETE_DTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return managerService.checkIsRelatedToRootParent(dto.ids, userAuthentication.tenantId!!)
    }

    // endregion

    // region Response shaping hooks (override to return VOs instead of raw entities)

    /** Shape the response body for `POST /query`. Default returns paginated entities. */
    protected suspend fun buildQueryResponse(dto: READ_DTO): Any {
        return managerService.query(dto)
    }

    /** Shape the response body for [readAll]. Default returns the entities of the tenant. */
    protected suspend fun buildReadAllResponse(tenantId: Long): Any {
        return managerService.findAllByTenantId(tenantId)
    }

    // endregion

    // region Custom-flow hooks (return non-null to short-circuit the standard path)

    /**
     * Hook for [readAll]. Return non-null to short-circuit the standard logic with a custom
     * response; return null (default) to fall through to the standard implementation.
     */
    protected suspend fun customReadAll(
        userAuthentication: UserAuthentication,
        tenantId: Long,
    ): ApiResponse<*>? = null

    /** Hook for [create] — see [customReadAll]. */
    protected suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: CREATE_DTO
    ): ApiResponse<*>? = null

    /** Hook for [read] (`POST /query`) — see [customReadAll]. */
    protected suspend fun customQuery(
        userAuthentication: UserAuthentication,
        dto: READ_DTO
    ): ApiResponse<*>? = null

    /** Hook for [update] — see [customReadAll]. */
    protected suspend fun customUpdate(
        userAuthentication: UserAuthentication,
        dto: UPDATE_DTO
    ): ApiResponse<*>? = null

    /** Hook for [delete] — see [customReadAll]. */
    protected suspend fun customDelete(
        userAuthentication: UserAuthentication,
        dto: DELETE_DTO
    ): ApiResponse<*>? = null

    // endregion

    // ─── AbstractManagerController hooks ───

    override suspend fun buildReadResponse(
        dto: READ_DTO,
        userAuthentication: UserAuthentication,
    ): Any = buildQueryResponse(dto)

    override suspend fun authorize(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: CREATE_DTO?,
        readDto: READ_DTO?,
        updateDto: UPDATE_DTO?,
        deleteDto: DELETE_DTO?,
    ) {
        when (action) {
            ManagerAction.CREATE -> authorizeCreate(userAuthentication, createDto!!)
            ManagerAction.READ -> authorizeRead(userAuthentication, readDto!!)
            ManagerAction.UPDATE -> authorizeUpdate(userAuthentication, updateDto!!)
            ManagerAction.DELETE -> authorizeDelete(userAuthentication, deleteDto!!)
            ManagerAction.READ_ALL -> Unit
        }
    }

    private suspend fun authorizeCreate(auth: UserAuthentication, dto: CREATE_DTO) {
        if (RbacUtils.hasAuthority(permissions.tenantAdminCreate)) return
        if (hasScopedAuthority(permissions.tenantPemCreate)) {
            auth.assertTenantIdNotNull()
            if (!isCreateInScope(dto, auth)) throw UnauthorizedException()
            return
        }
        throw ForbiddenException()
    }

    private suspend fun authorizeRead(auth: UserAuthentication, dto: READ_DTO) {
        if (RbacUtils.hasAuthority(permissions.tenantAdminRead)) return
        if (hasScopedAuthority(permissions.tenantPemRead)) {
            if (!isQueryInScope(dto, auth)) throw UnauthorizedException()
            return
        }
        throw ForbiddenException()
    }

    private suspend fun authorizeUpdate(auth: UserAuthentication, dto: UPDATE_DTO) {
        if (RbacUtils.hasAuthority(permissions.tenantAdminUpdate)) return
        if (hasScopedAuthority(permissions.tenantPemUpdate)) {
            auth.assertTenantIdNotNull()
            if (!isUpdateInScope(dto, auth)) throw UnauthorizedException()
            return
        }
        throw ForbiddenException()
    }

    private suspend fun authorizeDelete(auth: UserAuthentication, dto: DELETE_DTO) {
        if (RbacUtils.hasAuthority(permissions.tenantAdminDelete)) return
        if (hasScopedAuthority(permissions.tenantPemDelete)) {
            auth.assertTenantIdNotNull()
            if (!isDeleteInScope(dto, auth)) throw UnauthorizedException()
            return
        }
        throw ForbiddenException()
    }

    // ─── Preflight routing (customXxx short-circuit lives here, not on endpoint overrides) ───
    //
    // Overriding the endpoint methods themselves would trigger Kotlin bridge methods (this class
    // tightens READ_DTO to `BaseManagerReadTenantResourceDTO`, so the erased signature differs
    // from the parent) and Spring would register two `@PostMapping` handlers for the same URL.
    // Routing through [preflight] keeps the endpoint definitions single-sourced on the parent.

    override suspend fun preflight(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: CREATE_DTO?,
        readDto: READ_DTO?,
        updateDto: UPDATE_DTO?,
        deleteDto: DELETE_DTO?,
    ): ApiResponse<*>? = when (action) {
        ManagerAction.CREATE -> customCreate(userAuthentication, createDto!!)
        ManagerAction.READ -> customQuery(userAuthentication, readDto!!)
        ManagerAction.UPDATE -> customUpdate(userAuthentication, updateDto!!)
        ManagerAction.DELETE -> customDelete(userAuthentication, deleteDto!!)
        ManagerAction.READ_ALL -> null // readAll has its own endpoint on this class; customReadAll is invoked there
    }

    // ─── readAll endpoint (unique @RequestParam signature keeps it on this class) ───

    @GetMapping("/list", version = "1")
    open suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam tenantId: Long,
    ): ApiResponse<*> {
        customReadAll(userAuthentication, tenantId)?.let { return it }

        return if (RbacUtils.hasAuthority(permissions.tenantAdminRead)) {
            ApiResponse.success(buildReadAllResponse(tenantId))
        } else if (hasScopedAuthority(permissions.tenantPemRead)) {
            if (isReadAllInScope(tenantId, userAuthentication)) {
                ApiResponse.success(buildReadAllResponse(tenantId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }
}
