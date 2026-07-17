package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.service.ScopedRelationshipCheckService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
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
 * **This controller covers both directly scoped and derived-scoped resources** — the difference
 * lives at the Service layer via [ScopedRelationshipCheckService.resolveRootScope]:
 *
 *  - **Directly scoped entities** (implementing `BaseScopedEntity`): the default
 *    [BaseScopedManagerService.resolveRootScope] reads the entity's own `scope / scopeId` columns.
 *  - **Derived-scoped entities** (no scope columns of their own — e.g. `dict_item.type_id`): the
 *    Service impl overrides [ScopedRelationshipCheckService.resolveRootScope] to delegate to the
 *    scoped parent's Service. Deep chains compose recursively — the tenant side does the same
 *    thing via `checkIsRelatedToRootParent`.
 *
 * Authorization is driven by a four-layer [ScopedPermissionMatrix]:
 *
 *  1. [checkPermission] verifies the caller holds any of the layers eligible for the current
 *     `(scope, operation)` — SYSTEM consults super + system; TENANT consults super + tenantAdmin
 *     + tenantPem.
 *  2. [checkOwnership] additionally enforces tenant isolation for TENANT-scoped requests: a caller
 *     holding a cross-tenant layer (super or tenantAdmin) is allowed anywhere; otherwise the
 *     resolved root `scopeId` must equal `userAuthentication.tenantId`.
 *
 * The `(scope, scopeId)` for each operation is produced by three overridable hooks whose defaults
 * cover the common case:
 *
 *  - [resolveScopeFromCreateDTO] — DTO scoped fields (requires [BaseManagerCreateScopedDTO])
 *  - [resolveScopeFromReadDTO]   — DTO scoped fields (requires [BaseManagerReadScopedDTO])
 *  - [resolveScopeFromEntity]    — delegates to `managerService.resolveRootScope(entity.id)`
 *
 * Derived-scoped controllers whose DTOs do not carry `scope + scopeId` (they carry a parent id
 * such as `typeId` instead) MUST override the two DTO-resolving hooks to look up the parent's
 * root scope via the parent's Service.
 */
@Validated
abstract class StandardScopedManagerController<
        SERVICE,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
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
) where SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        SERVICE : ScopedRelationshipCheckService {

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
     * Ownership check. See class-level doc for the full rule; the short version: TENANT scope
     * requires a cross-tenant layer (super / tenantAdmin) OR `scopeId == tenantId`.
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

    /** Resolve [ResourceScope] from the raw scope typeId. */
    protected open fun resolveScope(scopeTypeId: Int): ResourceScope {
        return ResourceScope.getById(scopeTypeId)
            ?: throw IllegalArgumentException("Unknown scope type: $scopeTypeId")
    }

    // ─── Scope resolution hooks ───

    /**
     * Resolve the root `(scope, scopeId)` for a create DTO. Default expects the DTO to implement
     * [BaseManagerCreateScopedDTO] and reads its scope fields directly. Override when the DTO
     * carries a parent id instead (derived-scope case).
     */
    protected open suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long> {
        val scopedDto = dto as? BaseManagerCreateScopedDTO
            ?: throw BusinessException("Default resolveScopeFromCreateDTO requires the DTO to implement BaseManagerCreateScopedDTO. Override it for derived-scope DTOs.")
        return resolveScope(scopedDto.scope) to scopedDto.scopeId
    }

    /**
     * Resolve the root `(scope, scopeId)` for a read DTO. Default expects [BaseManagerReadScopedDTO];
     * override for derived-scope DTOs.
     */
    protected open suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long> {
        val scopedDto = dto as? BaseManagerReadScopedDTO
            ?: throw BusinessException("Default resolveScopeFromReadDTO requires the DTO to implement BaseManagerReadScopedDTO. Override it for derived-scope DTOs.")
        return resolveScope(scopedDto.scope) to scopedDto.scopeId
    }

    /**
     * Resolve the root `(scope, scopeId)` for an existing entity — delegates to the Service's
     * [ScopedRelationshipCheckService.resolveRootScope]. Directly scoped Service impls inherit
     * the default (read entity fields); derived Service impls must override that method to walk
     * up the parent chain.
     */
    protected open suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long> {
        return managerService.resolveRootScope(entity.id)
            ?: error("Could not resolve root scope for entity ${entity.id}. Ensure the Service overrides resolveRootScope for derived entities.")
    }

    // ─── Response shaping hooks ───

    /** Shape the response body for [query]. Default returns paginated entities. */
    protected open suspend fun buildQueryResponse(
        dto: READ_DTO,
        userAuthentication: UserAuthentication,
    ): Any {
        return managerService.query(dto)
    }

    /**
     * Shape the response body for [readAll]. Default calls the Service's `findAllByScopeId(scopeId)`
     * via reflection-friendly cast — only works when the Service implements
     * [BaseScopedManagerService]. Derived-scope controllers whose Service does not expose that
     * API should override this hook (or simply not expose the `/list` endpoint in their route
     * table).
     */
    protected open suspend fun buildReadAllResponse(scopeId: Long): Any {
        val scopedService = managerService as? BaseScopedManagerService<*, *, *, *, *, *>
            ?: throw UnsupportedOperationException("/list endpoint requires the Service to implement BaseScopedManagerService, or override buildReadAllResponse")
        @Suppress("UNCHECKED_CAST")
        return (scopedService as BaseScopedManagerService<REPOSITORY, *, *, *, *, *>).findAllByScopeId(scopeId)
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
        val (scope, scopeId) = resolveScopeFromCreateDTO(dto)
        assertAccess(scope, scopeId, ScopedOperation.CREATE, userAuthentication)
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
        val (scope, scopeId) = resolveScopeFromReadDTO(dto)
        assertAccess(scope, scopeId, ScopedOperation.READ, userAuthentication)
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
        val (scope, scopeId) = resolveScopeFromEntity(entity)
        assertAccess(scope, scopeId, ScopedOperation.UPDATE, userAuthentication)
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
        val resolved = entities.map { resolveScopeFromEntity(it) }
        resolved.toSet().forEach { (scope, scopeId) ->
            assertAccess(scope, scopeId, ScopedOperation.DELETE, userAuthentication)
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
