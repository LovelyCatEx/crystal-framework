/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.constants

enum class ChannelType(val typeId: Int) {
    EMAIL(1),
    LARK(2);

    companion object {
        fun fromTypeId(typeId: Int): ChannelType? = entries.firstOrNull { it.typeId == typeId }
    }
}
