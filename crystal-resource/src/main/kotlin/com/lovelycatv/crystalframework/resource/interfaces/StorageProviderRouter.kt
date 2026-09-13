/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity

interface StorageProviderRouter {
    suspend fun get(context: RoutingContext): StorageProviderEntity

    fun invalidateCache()
}