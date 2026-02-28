package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity> {
}