package com.lovelycatv.crystalframework.spi.message

import com.lovelycatv.crystalframework.sdk.message.config.ContactableUserProvider
import com.lovelycatv.crystalframework.sdk.message.config.ContactableUserView
import com.lovelycatv.crystalframework.shared.request.PageQuery
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

/**
 * Composition-root implementation of [ContactableUserProvider]: exact-match user lookup for
 * initiating SYSTEM-scope peer conversations. The messaging core stays user-module-agnostic;
 * this wiring happens in crystal-starter, backed by the user module.
 *
 * Policy: no browsable directory. A blank keyword returns nothing; a non-blank keyword resolves
 * the single user whose username or email equals it exactly, with the caller themselves filtered
 * out. Mirrors [DefaultContactableTenantProvider] so future per-user visibility rules land here.
 */
@Component
class DefaultContactableUserProvider(
    private val userRepository: UserRepository,
) : ContactableUserProvider {
    override suspend fun searchContactableUsers(
        currentUserId: Long,
        keyword: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<ContactableUserView> {
        val cappedPageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val trimmed = keyword?.trim()
        if (trimmed.isNullOrBlank()) {
            return PageQuery(page, cappedPageSize).toPaginatedResponseData(total = 0L, records = emptyList())
        }
        val match: UserEntity? = userRepository.findByUsernameOrEmail(trimmed, trimmed).awaitFirstOrNull()
        val views = listOfNotNull(match)
            .filter { it.id != currentUserId }
            .map { user ->
                ContactableUserView(
                    id = user.id.toString(),
                    username = user.username,
                    nickname = user.nickname,
                    avatar = user.avatar?.toString(),
                )
            }
        return PageQuery(page, cappedPageSize).toPaginatedResponseData(total = views.size.toLong(), records = views)
    }

    companion object {
        private const val MAX_PAGE_SIZE = 20
    }
}
