/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import reactor.core.publisher.Flux

interface AnnouncementService {
    fun getPublished(): Flux<AnnouncementEntity>
}
