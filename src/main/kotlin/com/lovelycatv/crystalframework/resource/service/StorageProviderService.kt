package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository

interface StorageProviderService : CachedBaseService<StorageProviderRepository, StorageProviderEntity> {
}