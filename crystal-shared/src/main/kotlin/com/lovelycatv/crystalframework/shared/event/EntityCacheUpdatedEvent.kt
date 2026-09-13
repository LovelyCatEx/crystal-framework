/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.event

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.EntityCacheEventType
import kotlin.reflect.KClass

data class EntityCacheUpdatedEvent(
    override val entityId: Long,
    override val entityClass: KClass<out BaseEntity>,
    override val cacheKey: String,
) : SingleEntityCacheEvent {
    override val eventType: EntityCacheEventType get() = EntityCacheEventType.UPDATED
}
