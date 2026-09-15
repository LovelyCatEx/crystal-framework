/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitDeclaration

class TenantBenefitRegistry {
    private val benefits = linkedMapOf<String, TenantBenefitDeclaration>()

    fun benefit(declaration: TenantBenefitDeclaration) {
        val featureKey = declaration.featureKey.trim()
        if (featureKey.isBlank()) {
            return
        }

        check(benefits.putIfAbsent(featureKey, declaration.copy(featureKey = featureKey)) == null) {
            "TenantBenefitRegistry: duplicate featureKey '$featureKey'"
        }
    }

    fun benefits(declarations: Iterable<TenantBenefitDeclaration>) {
        declarations.forEach { benefit(it) }
    }

    fun benefitDeclarations(): List<TenantBenefitDeclaration> {
        return benefits.values.toList()
    }
}
