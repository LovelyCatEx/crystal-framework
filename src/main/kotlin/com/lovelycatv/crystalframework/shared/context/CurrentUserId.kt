/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.shared.context

object CurrentUserId : ContextKey<Long>() {
    override val key: Any get() = this
}