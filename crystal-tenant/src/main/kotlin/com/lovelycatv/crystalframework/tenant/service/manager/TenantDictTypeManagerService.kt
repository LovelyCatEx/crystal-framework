/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictTypeRepository

interface TenantDictTypeManagerService : BaseScopedManagerService<
        TenantDictTypeRepository,
        TenantDictTypeEntity,
        ManagerCreateTenantDictTypeDTO,
        ManagerReadTenantDictTypeDTO,
        ManagerUpdateTenantDictTypeDTO,
        ManagerDeleteTenantDictTypeDTO
        > {
    /**
     * Resolve a dict type by its scope coordinates + business `code`. Returns `null` when no such
     * dict type exists in that scope. Read-only; used by cross-module callers that reference a
     * dict by code (e.g. approval DICT form fields).
     */
    suspend fun findByScopeAndCode(scope: ResourceScope, scopeId: Long, code: String): TenantDictTypeEntity?
}
