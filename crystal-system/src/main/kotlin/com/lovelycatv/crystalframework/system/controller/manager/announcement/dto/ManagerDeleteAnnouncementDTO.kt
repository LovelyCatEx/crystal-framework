/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

class ManagerDeleteAnnouncementDTO(
    override val ids: List<Long> = emptyList(),
) : BaseManagerDeleteDTO(ids)
