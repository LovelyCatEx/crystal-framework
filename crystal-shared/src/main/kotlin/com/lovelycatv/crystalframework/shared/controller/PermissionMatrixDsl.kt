/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.controller

/**
 * DSL marker for [PermissionMatrix]. Prevents accidental scope-leak between layer builders —
 * writing `create = ...` inside a `super { ... }` block cannot accidentally see the outer
 * matrix builder's members.
 */
@DslMarker
annotation class PermissionMatrixDsl
