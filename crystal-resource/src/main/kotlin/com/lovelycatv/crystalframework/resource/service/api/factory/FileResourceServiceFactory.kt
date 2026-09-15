/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService

/**
 * SPI for producing an [AbstractFileResourceService] bound to a specific [StorageProviderEntity].
 *
 * Dispatch is by `typeId: Int` (matched against [StorageProviderEntity.type]), so third-party
 * providers registered via `StorageProviderTypeRegistry` (typeId >= 1000) can plug in on equal
 * footing with the built-in ones. Do **not** switch on any enum here — the enum only exists as a
 * convenience alias for the built-in typeIds.
 */
interface FileResourceServiceFactory<S: AbstractFileResourceService> {
    fun getStorageProviderTypeId(): Int

    fun build(storageProvider: StorageProviderEntity): S
}
