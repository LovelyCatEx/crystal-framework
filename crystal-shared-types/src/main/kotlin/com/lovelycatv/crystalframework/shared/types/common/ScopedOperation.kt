/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.common

/**
 * Standard CRUD operations used by [StandardScopedManagerController] for permission delegation.
 */
enum class ScopedOperation {
    CREATE,
    READ,
    UPDATE,
    DELETE
}
