package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.service.manager.OAuthAccountManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/oauth-account")
class ManagerOAuthAccountController(
    managerService: OAuthAccountManagerService
) : StandardManagerController<
        OAuthAccountManagerService,
        OAuthAccountRepository,
        OAuthAccountEntity,
        ManagerCreateOAuthAccountDTO,
        ManagerReadOAuthAccountDTO,
        ManagerUpdateOAuthAccountDTO,
        ManagerDeleteOAuthAccountDTO
        >(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_OAUTH_ACCOUNT_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_OAUTH_ACCOUNT_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_OAUTH_ACCOUNT_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_OAUTH_ACCOUNT_DELETE.name,
    ),
)
