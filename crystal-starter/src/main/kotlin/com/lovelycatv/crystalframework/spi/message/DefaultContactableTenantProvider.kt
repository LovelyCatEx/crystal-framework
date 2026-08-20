package com.lovelycatv.crystalframework.spi.message

import com.lovelycatv.crystalframework.sdk.message.config.ContactableTenantProvider
import com.lovelycatv.crystalframework.sdk.message.config.ContactableTenantView
import com.lovelycatv.crystalframework.shared.request.PageQuery
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

/**
 * Composition-root implementation of [ContactableTenantProvider]: exposes a paginated
 * tenant directory for initiating customer-service conversations. The messaging core
 * stays tenant-agnostic; this wiring happens in crystal-starter.
 *
 * Current policy: any logged-in user may contact any tenant (no visibility filtering).
 * The "not走 managercontroller" design makes future per-user visibility controls easy —
 * just inject a visibility service and filter here.
 */
@Component
class DefaultContactableTenantProvider(
    private val tenantRepository: TenantRepository,
) : ContactableTenantProvider {
    override suspend fun pageContactableTenants(
        keyword: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<ContactableTenantView> {
        val cappedPageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), cappedPageSize)

        val tenants: List<TenantEntity>
        val total: Long
        if (keyword.isNullOrBlank()) {
            tenants = tenantRepository.findAllBy(pageable).collectList().awaitFirstOrNull().orEmpty()
            total = tenantRepository.count().awaitFirstOrNull() ?: 0L
        } else {
            tenants = tenantRepository.findAllByNameContainingIgnoreCase(keyword, pageable).collectList().awaitFirstOrNull().orEmpty()
            total = tenantRepository.countByNameContainingIgnoreCase(keyword).awaitFirstOrNull() ?: 0L
        }

        val views = tenants.map { tenant -> ContactableTenantView(id = tenant.id.toString(), name = tenant.name) }
        return PageQuery(page, cappedPageSize).toPaginatedResponseData(total = total, records = views)
    }

    companion object {
        private const val MAX_PAGE_SIZE = 20
    }
}
