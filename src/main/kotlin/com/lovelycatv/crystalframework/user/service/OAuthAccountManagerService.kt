package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerDeleteOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerReadOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface OAuthAccountManagerService : CachedBaseManagerService<
        OAuthAccountRepository,
        OAuthAccountEntity,
        ManagerCreateOAuthAccountDTO,
        ManagerReadOAuthAccountDTO,
        ManagerUpdateOAuthAccountDTO,
        ManagerDeleteOAuthAccountDTO
> {
    override suspend fun query(
        dto: ManagerReadOAuthAccountDTO,
        isAdvanceQuery: suspend (dto: ManagerReadOAuthAccountDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadOAuthAccountDTO, limit: Int, offset: Int) -> PaginatedResponseData<OAuthAccountEntity>
    ): PaginatedResponseData<OAuthAccountEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.platform != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.platform,
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.platform,
                    limit,
                    offset
                ).awaitListWithTimeout()

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        )
    }
}
