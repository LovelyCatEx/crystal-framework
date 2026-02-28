package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import org.springframework.stereotype.Service

@Service
class StorageProviderServiceImpl(
    private val storageProviderRepository: StorageProviderRepository
) : StorageProviderService {
    override fun getRepository(): StorageProviderRepository {
        return this.storageProviderRepository
    }
}