/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.event

enum class LoginMethod(val code: Int) {
    PASSWORD(0),
    OAUTH2(1);

    companion object {
        fun getByCode(code: Int): LoginMethod? {
            return entries.find { it.code == code }
        }
    }
}