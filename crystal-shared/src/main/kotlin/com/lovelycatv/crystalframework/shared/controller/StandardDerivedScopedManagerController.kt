package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Base controller for scope-aware resources whose entity does **not** carry its own
 * `scope` / `scope_id` columns — instead, scope is derived from a parent entity along
 * the relationship chain (e.g. `dict_item.type_id → dict_type.scope/scope_id`).
 *
 * Conceptually this is the "derived-scope" counterpart of [StandardScopedManagerController].
 * Instead of relying on [com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity]
 * fields, subclasses tell the framework how to resolve a `(scope, scopeId)` pair from
 * each DTO / entity via the three abstract hooks below:
 *
 * - [resolveScopeFromCreateDTO] — for POST `/create`
 * - [resolveScopeFromReadDTO]   — for POST `/query`
 * - [resolveScopeFromEntity]    — for POST `/update` and `/delete`
 *
 * Authorization is then driven uniformly by the supplied [ScopedPermissionTriad]:
 * `hasAnyAuthority(super<op>, scopeSpecific<op>)`, where `scopeSpecific` is `system<op>`
 * for SYSTEM and `tenantPem<op>` for TENANT.
 *
 * Ownership is enforced via [checkOwnership]: TENANT-scoped resources require
 * `scopeId == userAuthentication.tenantId`; SYSTEM-scoped passes by default.
 *
 * Provides POST `/create`, `/query`, `/update`, `/delete` endpoints. The `/list` endpoint
 * is intentionally omitted because there is no straightforward way to enumerate all
 * derived-scope rows without a parent context. Subclasses needing list semantics should
 * add their own custom endpoint.
 */
@Validated
abstract class StandardDerivedScopedManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE,
    protected val permissions: ScopedPermissionTriad,
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<*> {

    // ─── Abstract: subclass must implement scope resolution ───

    /**
     * Resolve `(scope, scopeId)` for a create DTO. Typically looks up the parent entity
     * referenced by a foreign key on the DTO and returns its scope.
     */
    protected abstract suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long>

    /**
     * Resolve `(scope, scopeId)` for a read DTO. Typically looks up the parent entity
     * referenced by a foreign key on the DTO and returns its scope.
     */
    protected abstract suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long>

    /**
     * Resolve `(scope, scopeId)` for an existing entity (used by update / delete).
     * Typically walks up to the parent entity via the relationship chain.
     */
    protected abstract suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long>

    // ─── Overridable hooks ───

    /**
     * Ownership check.
     *
     * Defaults:
     * - SYSTEM: passes.
     * - TENANT: holders of the **super** authority for [operation] (cross-scope admins)
     *   bypass the tenant check; otherwise `scopeId == tenantId` is required.
     *
     * The super-authority bypass is op-scoped: a user holding only `super.read` may read
     * across tenants but cannot update across tenants without also holding `super.update`.
     */
    protected open suspend fun checkOwnership(
        scope: ResourceScope,
        scopeId: Long,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return when (scope) {
            ResourceScope.SYSTEM -> true
            ResourceScope.TENANT -> {
                if (RbacUtils.hasAuthority(permissions.superFor(operation))) {
                    true
                } else {
                    scopeId == userAuthentication.tenantId
                }
            }
        }
    }

    /** Shape the response body for [query]. Default returns paginated entities. */
    protected open suspend fun buildQueryResponse(
        dto: READ_DTO,
        userAuthentication: UserAuthentication,
    ): Any {
        return managerService.query(dto)
    }

    // ─── Endpoints ───

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
        // Group by resolved scope, check each distinct scope group once.
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
        scopeId: Long,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ) {
        if (!RbacUtils.hasAnyAuthority(*permissions.forScope(scope, operation))) {
            throw ForbiddenException()
        }
        if (!checkOwnership(scope, scopeId, operation, userAuthentication)) {
            throw UnauthorizedException()
        }
    }
}
