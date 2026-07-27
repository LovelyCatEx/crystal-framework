package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Common ancestor for the standardised manager controller family (Standard / Scoped / Tenant and
 * their Readonly variants). Owns the four DTO-shaped CRUD endpoints — `POST /create`, `POST /query`
 * (method name [read]), `POST /update`, `POST /delete` — so URL, HTTP verb, parameter binding,
 * validation, and [ApiResponse] wrapping live in exactly one place.
 *
 * `GET /list` (readAll) is intentionally NOT defined here: the three concrete main lines each accept
 * a different `@RequestParam` shape (none / `scope + scopeId` / `tenantId`) and Spring MVC cannot
 * bind those from a single inherited signature. Each subclass declares its own `readAll`.
 *
 * Subclasses customise behaviour through three protected hooks:
 *  - [preflight] — first thing each endpoint runs. Return a non-null [ApiResponse] to short-circuit
 *    the entire pipeline (skip mutability, authorize, and the service call). Default returns null.
 *    Tenant main line overrides this to route to its `customXxx` hooks. Overriding the endpoint
 *    methods directly is unsafe when the subclass tightens a DTO generic bound — Kotlin generates
 *    bridge methods that collide with Spring's `@PostMapping` registration; go through [preflight]
 *    instead.
 *  - [authorize] — invoked after [preflight] on every write and before [read]. Default is a no-op
 *    (Standard main line delegates permission checks to `ManagerControllerPermissionAspect` /
 *    `@ManagerPermissions`). Scoped/Tenant main lines override this to run their in-line permission
 *    logic.
 *  - [buildReadResponse] — shapes the `POST /query` response. Default returns paginated entities via
 *    `managerService.query(dto)`; subclasses override to inject scope filtering or return VOs.
 *
 * The [mutability] constructor parameter lets Readonly variants forbid writes with a single line
 * (`mutability = Mutability.READ_ONLY`) instead of three manual endpoint overrides.
 *
 * Note: this class is annotated `@Validated` but not `@RestController` / `@RequestMapping` — those
 * belong on the concrete subclass that becomes a Spring bean.
 */
@Validated
abstract class AbstractManagerController<
        SERVICE,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE,
    protected val mutability: Mutability = Mutability.READ_WRITE,
) where SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO> {

    /**
     * Short-circuit hook invoked at the very start of every endpoint (before [mutability],
     * [authorize], and the service call). Return a non-null [ApiResponse] to bypass the standard
     * pipeline entirely; return null (default) to fall through.
     *
     * This hook exists so subclasses whose DTO generic bounds are stricter than the parent's can
     * inject custom behaviour without overriding the endpoint methods themselves — Kotlin would
     * emit bridge methods carrying the inherited `@PostMapping`, and Spring registers those as
     * duplicate handlers. Route custom flows through [preflight] instead.
     */
    protected open suspend fun preflight(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: CREATE_DTO? = null,
        readDto: READ_DTO? = null,
        updateDto: UPDATE_DTO? = null,
        deleteDto: DELETE_DTO? = null,
    ): ApiResponse<*>? = null

    /**
     * Permission decision hook. Default is a no-op — the Standard main line relies on
     * `ManagerControllerPermissionAspect` + `@ManagerPermissions` for authorisation. Scoped/Tenant
     * main lines override this to run in-line permission checks and throw on denial.
     *
     * All four DTOs are passed as nullable parameters so the override can branch on [action] and
     * dereference only the relevant DTO. `readAll` invokes this with all DTOs null when applicable.
     */
    protected open suspend fun authorize(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: CREATE_DTO? = null,
        readDto: READ_DTO? = null,
        updateDto: UPDATE_DTO? = null,
        deleteDto: DELETE_DTO? = null,
    ) = Unit

    /**
     * Shape the response body for [read] (`POST /query`). Default returns paginated entities from
     * the service. Subclasses override to inject scope filtering, return VOs, or add ownership
     * projections.
     */
    protected open suspend fun buildReadResponse(
        dto: READ_DTO,
        userAuthentication: UserAuthentication,
    ): Any = managerService.query(dto)

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: CREATE_DTO,
    ): ApiResponse<*> {
        preflight(ManagerAction.CREATE, userAuthentication, createDto = dto)?.let { return it }
        mutability.assertCreateAllowed()
        authorize(ManagerAction.CREATE, userAuthentication, createDto = dto)
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query", version = "1")
    suspend fun read(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: READ_DTO,
    ): ApiResponse<*> {
        preflight(ManagerAction.READ, userAuthentication, readDto = dto)?.let { return it }
        authorize(ManagerAction.READ, userAuthentication, readDto = dto)
        return ApiResponse.success(buildReadResponse(dto, userAuthentication))
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UPDATE_DTO,
    ): ApiResponse<*> {
        preflight(ManagerAction.UPDATE, userAuthentication, updateDto = dto)?.let { return it }
        mutability.assertUpdateAllowed()
        authorize(ManagerAction.UPDATE, userAuthentication, updateDto = dto)
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: DELETE_DTO,
    ): ApiResponse<*> {
        preflight(ManagerAction.DELETE, userAuthentication, deleteDto = dto)?.let { return it }
        mutability.assertDeleteAllowed()
        authorize(ManagerAction.DELETE, userAuthentication, deleteDto = dto)
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
