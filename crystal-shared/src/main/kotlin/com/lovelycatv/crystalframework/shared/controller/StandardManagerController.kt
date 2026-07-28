package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.GetMapping

/**
 * Standard manager controller with the four CRUD endpoints inherited from
 * [AbstractManagerController] and a `GET /list` endpoint that returns every row.
 *
 * Pass a [PermissionMatrix] to the `permissions` constructor parameter — [authorize] will OR-check
 * `matrix.layersFor(SYSTEM, op)` against the caller's authorities. If a subclass leaves the
 * parameter null, `ManagerControllerPermissionAspect` denies every request by default so an
 * unconfigured controller cannot serve traffic undetected.
 *
 * Use [PermissionMatrix.systemOnly] / [PermissionMatrix.systemOnlyReadonly] convenience factories
 * for SYSTEM-only resources; use the `PermissionMatrix.of {}` DSL when a resource participates in
 * multiple layers.
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
    /**
     * Unified permission matrix. Standard resources are SYSTEM-scoped, so [authorize] consults
     * `matrix.layersFor(SYSTEM, op)`. Leave null and the AOP safety net denies every request.
     */
    protected val permissions: PermissionMatrix? = null,
    mutability: Mutability = Mutability.READ_WRITE,
) : AbstractManagerController<SERVICE, REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>(
    managerService,
    mutability,
) {
    override suspend fun authorize(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: CREATE_DTO?,
        readDto: READ_DTO?,
        updateDto: UPDATE_DTO?,
        deleteDto: DELETE_DTO?,
    ) {
        val matrix = permissions ?: return
        val op = when (action) {
            ManagerAction.CREATE -> ScopedOperation.CREATE
            ManagerAction.READ -> ScopedOperation.READ
            ManagerAction.READ_ALL -> ScopedOperation.READ
            ManagerAction.UPDATE -> ScopedOperation.UPDATE
            ManagerAction.DELETE -> ScopedOperation.DELETE
        }
        val required = matrix.layersFor(ResourceScope.SYSTEM, op)
        if (!RbacUtils.hasAnyAuthority(*required)) {
            throw AuthorizationDeniedException("Access denied: required any of ${required.toList()}")
        }
    }

    @GetMapping("/list", version = "1")
    open suspend fun readAll(
        userAuthentication: UserAuthentication,
    ): ApiResponse<*> {
        // readAll has its own endpoint here (no shared parent signature); permission check runs inline.
        authorize(ManagerAction.READ_ALL, userAuthentication)
        return ApiResponse.success(managerService.getRepository().findAll().awaitListWithTimeout())
    }
}
