package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerDeleteOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerReadOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository

interface OAuthAccountManagerService : CachedBaseManagerService<
        OAuthAccountRepository,
        OAuthAccountEntity,
        ManagerCreateOAuthAccountDTO,
        ManagerReadOAuthAccountDTO,
        ManagerUpdateOAuthAccountDTO,
        ManagerDeleteOAuthAccountDTO
>
