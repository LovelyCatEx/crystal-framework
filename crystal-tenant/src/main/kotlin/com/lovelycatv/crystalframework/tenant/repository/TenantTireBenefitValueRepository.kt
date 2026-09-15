/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantTireBenefitValueRepository : BaseRepository<TenantTireBenefitValueEntity> {
    fun findByTireTypeId(tireTypeId: Long): Flux<TenantTireBenefitValueEntity>

    fun findByTireTypeIdIn(tireTypeIds: Collection<Long>): Flux<TenantTireBenefitValueEntity>

    fun findByTireTypeIdAndFeatureId(tireTypeId: Long, featureId: Long): Mono<TenantTireBenefitValueEntity>

    fun deleteByFeatureIdIn(featureIds: Collection<Long>): Mono<Void>
}
