/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository

interface TenantDepartmentService : CachedBaseService<TenantDepartmentRepository, TenantDepartmentEntity>
