/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateTenantTireBenefitValueDTO(
    override val id: Long,
    val tireTypeId: Long? = null,
    val featureId: Long? = null,
    val featureValue: String? = null,
) : BaseManagerUpdateDTO(id)
