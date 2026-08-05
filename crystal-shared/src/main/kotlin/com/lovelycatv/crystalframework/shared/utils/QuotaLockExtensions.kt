/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class QuotaLockExtensions private constructor()

/**
 * Serializes concurrent quota-guarded writes for a single owner row by taking a row-level
 * `FOR UPDATE` lock on the anchor row identified by ([tableName], [rowId]) inside the current
 * transaction. The lock is held until the surrounding transaction commits or rolls back, so any
 * concurrent transaction targeting the same row blocks until this one finishes.
 *
 * This closes the count-then-insert race behind tenant quota checks (member / invitation limits):
 * the anchor row (e.g. the tenant row) always exists, so contenders queue on it and re-read the
 * count only after the predecessor has committed — unlike locking the counted rows themselves,
 * which cannot lock rows that do not exist yet (an empty tenant would lock nothing).
 *
 * [tableName] must come from [com.lovelycatv.crystalframework.shared.constants.TableConstants]
 * (never a caller-supplied string) — it is interpolated into the statement because a table name
 * cannot be a bind parameter; [rowId] is always bound. The global soft-delete SQL interceptor
 * appends `deleted_time IS NULL` to this SELECT, so only a live anchor row is locked.
 *
 * Returns the id of the locked row, or `null` when no live row matched (nothing was locked). Quota
 * callers can ignore the result; it is surfaced so tests can assert a real row was actually locked
 * (guarding against the interceptor ever appending the inverted `deleted_time IS NOT NULL`, which
 * would silently match zero rows and defeat the serialization).
 */
suspend fun R2dbcEntityTemplate.lockRowForUpdate(tableName: String, rowId: Long): Long? {
    return this.databaseClient
        .sql("SELECT id FROM $tableName WHERE id = :id FOR UPDATE")
        .bind("id", rowId)
        .fetch()
        .first()
        .awaitFirstOrNull()
        ?.get(BaseEntity.COLUMN_ID)
        ?.let { (it as Number).toLong() }
}
