package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository

interface TenantRoleService : CachedBaseService<TenantRoleRepository, TenantRoleEntity>
