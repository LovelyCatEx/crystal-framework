/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy.types

/**
 * A pluggable "reference type" for an economy transaction — the external business that triggered
 * it (e.g. an AI model invocation, an order, a task reward). Contributed to
 * [com.lovelycatv.crystalframework.sdk.economy.EconomyReferenceTypeRegistry]; [typeId] is what
 * lands in `economy_transactions.reference_type`.
 *
 * Third parties must pick a [typeId] outside the framework's reserved range (`>= 1000`) and
 * namespace [key] under their vendor id.
 */
interface EconomyReferenceTypeDeclaration {
    /** Stable integer written to `economy_transactions.reference_type`. Built-in: 0..1; third-party: >= 1000. */
    val typeId: Int

    /** Globally-unique string identifier. Must NOT contain `.`; use `_` or `-` between words. */
    val key: String

    /** Human-readable label used by admin UIs. */
    val displayName: String

    /** Long-form description; safe to be empty. */
    val description: String
        get() = ""
}
