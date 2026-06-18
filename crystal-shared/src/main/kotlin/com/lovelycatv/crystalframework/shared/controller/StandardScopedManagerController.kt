package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Base controller for scope-aware manager endpoints (create / query / list / update / delete).
 *
 * Unlike [StandardTenantManagerController] which assumes a fixed dual-layer (system + tenant)
 * permission model, this controller receives an explicit `scope` + `scopeId` pair and delegates
 * all authorization decisions to the subclass via:
 *
 *  1. [checkPermission] — given scope + scopeId + operation, decide whether the user is authorized.
 *  2. [checkOwnership] (optional override) — verify the user can access data in this scope.
 *
 * The `scope` field is the [ResourceScope.typeId] (e.g. 0=SYSTEM, 1=TENANT).
 * The `scopeId` field is the concrete ID within that scope (e.g. tenantId when scope=TENANT).
 *
 * For update/delete, the scope is read directly from the entity's [BaseScopedEntity.scope]
 * and [BaseScopedEntity.scopeId] fields.
 *
 * Data isolation is handled at the service layer: [BaseScopedManagerService.buildQueryCriteria]
 * automatically injects scope-based filtering when scopeId is non-null.
 */
@Validated
abstract class StandardScopedManagerController<
        SERVICE : BaseScopedManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseScopedEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadScopedDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE,
    /**
     * Optional three-layer permission declaration. When provided, the default
     * [checkPermission] implementation is driven by this triad and subclasses no longer
     * need to override [checkPermission]. When null, subclasses must override
     * [checkPermission] to implement custom authorization logic.
     */
    protected val permissions: ScopedPermissionTriad? = null,
) {

    // ─── Permission decision ───

    /**
     * Permission decision callback.
     *
     * Default behavior:
     * - When [permissions] is non-null, performs `hasAnyAuthority(super<op>, scopeSpecific<op>)`
     *   based on the [ScopedPermissionTriad] supplied at construction.
     * - When [permissions] is null, throws [IllegalStateException] — subclasses without a
     *   triad MUST override this method.
     *
     * Override directly when custom authorization logic is needed (e.g. always-allow READ,
     * dynamic conditions, etc.).
     */
    protected open suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        val triad = permissions
            ?: error("StandardScopedManagerController#checkPermission must be overridden when no ScopedPermissionTriad is supplied")
        return RbacUtils.hasAnyAuthority(*triad.forScope(scope, operation))
    }

    // ─── Overridable hooks ───

    /**
     * Ownership check: verify that the user is allowed to access data within this scope.
     *
     * Default behavior:
     * - [ResourceScope.SYSTEM]: always passes (permission check is sufficient).
     * - [ResourceScope.TENANT]: holders of the **super** authority for [operation] (cross-scope
     *   admins) bypass the tenant check; otherwise [scopeId] must equal the user's tenantId.
     *
     * The super-authority bypass is op-scoped: a user holding only `super.read` may read
     * across tenants, but cannot update across tenants without also holding `super.update`.
     *
     * Override for custom ownership logic (e.g. nested resources).
     */
    protected open suspend fun checkOwnership(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return when (scope) {
            ResourceScope.SYSTEM -> true
            ResourceScope.TENANT -> {
                val triad = permissions
                if (triad != null && RbacUtils.hasAuthority(triad.superFor(operation))) {
                    true
                } else {
                    scopeId == userAuthentication.tenantId
                }
            }
        }
    }

    /**
     * Resolve [ResourceScope] from the raw scope typeId sent by the client.
     * Default delegates to [ResourceScope.getById].
     */
    protected open fun resolveScope(scopeTypeId: Int): ResourceScope {
        return ResourceScope.getById(scopeTypeId)
            ?: throw IllegalArgumentException("Unknown scope type: $scopeTypeId")
    }

    // ─── Response shaping hooks ───

    /** Shape the response body for [query]. Default returns paginated entities. */
    protected open suspend fun buildQueryResponse(
        dto: READ_DTO,
        userAuthentication: UserAuthentication,
    ): Any {
        return managerService.query(dto)
    }

    /** Shape the response body for [readAll]. Default returns all entities for the scope. */
    protected open suspend fun buildReadAllResponse(scopeId: Long): Any {
        return managerService.findAllByScopeId(scopeId)
    }

    // ─── Endpoints ───

    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam scope: Int,
        @RequestParam scopeId: Long,
    ): ApiResponse<*> {
        val resolvedScope = resolveScope(scope)
        assertAccess(resolvedScope, scopeId, ScopedOperation.READ, userAuthentication)
        return ApiResponse.success(buildReadAllResponse(scopeId))
    }

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: CREATE_DTO
    ): ApiResponse<*> {
        val scopedDto = dto as BaseManagerCreateScopedDTO
        val resolvedScope = resolveScope(scopedDto.scope)
        assertAccess(resolvedScope, scopedDto.scopeId, ScopedOperation.CREATE, userAuthentication)
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: READ_DTO
    ): ApiResponse<*> {
        val resolvedScope = resolveScope(dto.scope)
        assertAccess(resolvedScope, dto.scopeId, ScopedOperation.READ, userAuthentication)
        return ApiResponse.success(buildQueryResponse(dto, userAuthentication))
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UPDATE_DTO
    ): ApiResponse<*> {
        val entity = managerService.getByIdOrThrow(dto.id)
        val resolvedScope = resolveScope(entity.scope)
        assertAccess(resolvedScope, entity.scopeId, ScopedOperation.UPDATE, userAuthentication)
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
        val entities = dto.ids.map { managerService.getByIdOrThrow(it) }

        // Check permission and ownership for each distinct scope group
        entities.groupBy { it.scope to it.scopeId }.keys.forEach { (scopeType, scopeId) ->
            val resolvedScope = resolveScope(scopeType)
            assertAccess(resolvedScope, scopeId, ScopedOperation.DELETE, userAuthentication)
        }

        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }

    // ─── Internal ───

    private suspend fun assertAccess(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ) {
        if (!checkPermission(scope, scopeId, operation, userAuthentication)) {
            throw ForbiddenException()
        }
        if (!checkOwnership(scope, scopeId, operation, userAuthentication)) {
            throw UnauthorizedException()
        }
    }
}
