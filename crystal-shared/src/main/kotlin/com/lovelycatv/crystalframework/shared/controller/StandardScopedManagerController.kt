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
 * Authorization is driven by a four-layer [ScopedPermissionMatrix]:
 *
 *  1. [checkPermission] verifies the caller holds **any** of the layers eligible for the current
 *     `(scope, operation)` — SYSTEM consults super + system; TENANT consults super + tenantAdmin
 *     + tenantPem.
 *  2. [checkOwnership] additionally enforces tenant isolation for TENANT-scoped requests: a caller
 *     holding a cross-tenant layer (super or tenantAdmin) is allowed anywhere; otherwise the
 *     `scopeId` on the request must equal `userAuthentication.tenantId`.
 *
 * For update/delete, scope is read directly from the entity's [BaseScopedEntity] columns (not
 * from the client-supplied DTO) so a caller cannot lie about which tenant they are targeting.
 *
 * Data isolation is also enforced at the service layer: [BaseScopedManagerService.buildQueryCriteria]
 * injects scope-based SQL filtering when scopeId is non-null.
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
     * Four-layer permission matrix (super / system / tenantAdmin / tenantPem). When non-null the
     * default [checkPermission] and [checkOwnership] use it; when null, subclasses MUST override
     * both hooks.
     */
    protected val permissions: ScopedPermissionMatrix? = null,
) {

    // ─── Permission decision ───

    /**
     * Default: `hasAnyAuthority` across the layers eligible for the current `(scope, operation)`.
     * Override for special cases (e.g. `operation == READ` unconditionally, or dynamic rules).
     */
    protected open suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        val matrix = permissions
            ?: error("StandardScopedManagerController#checkPermission must be overridden when no ScopedPermissionMatrix is supplied")
        return RbacUtils.hasAnyAuthority(*matrix.layersFor(scope, operation))
    }

    // ─── Overridable hooks ───

    /**
     * Ownership check.
     *
     * Default behavior:
     * - [ResourceScope.SYSTEM]: always passes (permission check is sufficient — there is no
     *   "primary id" concept inside SYSTEM).
     * - [ResourceScope.TENANT]: holders of a **cross-tenant** layer (super or tenantAdmin) for
     *   [operation] bypass the tenant check; otherwise [scopeId] must equal
     *   `userAuthentication.tenantId`. The cross-tenant bypass is op-scoped: `super.read` alone
     *   does not authorize cross-tenant `update`.
     *
     * Override for custom logic (e.g. nested resources whose ownership requires a service lookup
     * beyond a plain equality check).
     */
    protected open suspend fun checkOwnership(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        val matrix = permissions ?: return true
        return when (scope) {
            ResourceScope.SYSTEM -> true
            ResourceScope.TENANT -> {
                if (RbacUtils.hasAnyAuthority(*matrix.crossTenantLayersFor(operation))) {
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
