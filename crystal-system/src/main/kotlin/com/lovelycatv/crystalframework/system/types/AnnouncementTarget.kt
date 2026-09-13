/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.types

enum class AnnouncementTarget(val code: Int) {
    USER_ONLY(0),
    MANAGER_ONLY(1),
    BOTH(2);

    companion object {
        fun fromCode(code: Int): AnnouncementTarget =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown announcement target code: $code")
    }
}
