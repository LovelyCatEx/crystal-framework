package com.lovelycatv.crystalframework.mail.controller.manager.type

import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerDeleteMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerReadMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateTypeManagerService
import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-template-type")
class ManagerMailTemplateTypeController(
    private val mailTemplateTypeManagerService: MailTemplateTypeManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(mailTemplateTypeManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateMailTemplateTypeDTO
    ): ApiResponse<*> {
        mailTemplateTypeManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadMailTemplateTypeDTO
    ): ApiResponse<*> {
        return ApiResponse.success(mailTemplateTypeManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateMailTemplateTypeDTO
    ): ApiResponse<*> {
        mailTemplateTypeManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteMailTemplateTypeDTO
    ): ApiResponse<*> {
        mailTemplateTypeManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
