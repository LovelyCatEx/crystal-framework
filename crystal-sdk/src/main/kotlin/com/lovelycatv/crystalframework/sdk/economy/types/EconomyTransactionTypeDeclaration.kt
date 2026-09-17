/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy.types

/**
 * A pluggable economy transaction "type" contributed to
 * [com.lovelycatv.crystalframework.sdk.economy.EconomyTransactionTypeRegistry].
 *
 * The framework's built-in types implement this interface directly on their enum values so they and
 * third-party contributions flow through the same lookup path; [typeId] is what lands in
 * `economy_transactions.type`.
 *
 * Third parties must pick a [typeId] outside the framework's reserved range (`>= 1000`) and
 * namespace [key] under their vendor id (e.g. `"acme_transfer"`). `typeId` values are frozen once
 * rows reference them — changing one breaks existing ledger records.
 */
interface EconomyTransactionTypeDeclaration {
    /** Stable integer written to `economy_transactions.type`. Built-in: 0..1; third-party: >= 1000. */
    val typeId: Int

    /** Globally-unique string identifier. Must NOT contain `.`; use `_` or `-` between words. */
    val key: String

    /** Human-readable label used by admin UIs. */
    val displayName: String

    /** Long-form description; safe to be empty. */
    val description: String
        get() = ""
}
