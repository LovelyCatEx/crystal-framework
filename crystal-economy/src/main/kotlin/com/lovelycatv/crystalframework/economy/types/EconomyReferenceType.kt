/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.types

import com.lovelycatv.crystalframework.sdk.economy.types.EconomyReferenceTypeDeclaration

/**
 * Built-in reference types for economy transactions — the external business that triggered a
 * ledger entry. The enum stays for compile-time convenience; runtime dispatch goes through
 * `EconomyReferenceTypeRegistry` so third-party types participate on equal footing.
 *
 * Only `NONE` (no reference) and `AI_INVOCATION` (AI model invocation charging) are reserved
 * here; anything else is contributed by developers with a `typeId >= 1000`.
 */
enum class EconomyReferenceType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
) : EconomyReferenceTypeDeclaration {
    NONE(
        typeId = 0,
        key = "builtin_none",
        displayName = "None",
        description = "No external business reference.",
    ),
    AI_INVOCATION(
        typeId = 1,
        key = "builtin_ai_invocation",
        displayName = "AI Invocation",
        description = "Charge for an AI model invocation.",
    ),
    ;

    companion object {
        fun getByTypeId(typeId: Int): EconomyReferenceType? = entries.find { it.typeId == typeId }
    }
}
