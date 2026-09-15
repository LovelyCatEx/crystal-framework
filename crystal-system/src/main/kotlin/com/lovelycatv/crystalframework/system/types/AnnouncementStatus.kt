/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.types

enum class AnnouncementStatus(val code: Int) {
    DRAFT(0),
    PUBLISHED(1),
    OFFLINE(2);

    companion object {
        fun fromCode(code: Int): AnnouncementStatus =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown announcement status code: $code")
    }
}
