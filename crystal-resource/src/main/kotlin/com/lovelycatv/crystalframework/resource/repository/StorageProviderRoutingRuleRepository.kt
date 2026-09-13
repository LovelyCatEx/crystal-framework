/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StorageProviderRoutingRuleRepository : BaseRepository<StorageProviderRoutingRuleEntity> {
    fun findAllByEnabledOrderByPriorityAsc(enabled: Boolean): Flux<StorageProviderRoutingRuleEntity>
}
