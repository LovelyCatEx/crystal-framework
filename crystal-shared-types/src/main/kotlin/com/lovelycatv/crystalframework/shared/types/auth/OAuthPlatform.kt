/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.auth

enum class OAuthPlatform(val typeId: Int) {
    GITHUB(0),
    GOOGLE(1),
    OICQ(2);

    companion object {
        fun getByTypeId(typeId: Int): OAuthPlatform? {
            return entries.find { it.typeId == typeId }
        }
    }
}