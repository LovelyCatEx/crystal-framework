/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types

enum class TenantBenefitType(val typeId: Int) {
    BOOLEAN(0),
    LIMIT(1),
    ENUM(2),
}
