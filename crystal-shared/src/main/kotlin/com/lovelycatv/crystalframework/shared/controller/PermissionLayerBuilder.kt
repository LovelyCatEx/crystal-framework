/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.controller

/**
 * Per-layer builder used inside [PermissionMatrixBuilder]. Each field defaults to
 * [PermissionMatrix.NOT_APPLICABLE] so writing only the operations the layer actually needs
 * (typical: just `read` for readonly variants) fills the remaining slots as "layer does not apply".
 */
@PermissionMatrixDsl
class PermissionLayerBuilder {
    var create: String = PermissionMatrix.NOT_APPLICABLE
    var read: String = PermissionMatrix.NOT_APPLICABLE
    var update: String = PermissionMatrix.NOT_APPLICABLE
    var delete: String = PermissionMatrix.NOT_APPLICABLE
}
