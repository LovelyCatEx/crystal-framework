package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.StorageProviderType

interface FileResourceServiceFactory<S: AbstractFileResourceService> {
    fun getStorageProviderType(): StorageProviderType

    fun build(storageProvider: StorageProviderEntity): S
}