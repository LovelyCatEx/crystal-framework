/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.vo

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Read-only projection of [ApprovalFlowRecordEntity] for the instance-details endpoint.
 *
 * Long ids are serialised as strings to match the Long-to-String convention documented in
 * `CLAUDE.md` (Controller → Long serialization).
 */
data class ApprovalFlowRecordVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val scope: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val scopeId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val instanceId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val nodeId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val operatorId: Long,
    val action: Int,
    val comment: String?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val createdTime: Long,
) {
    companion object {
        fun from(entity: ApprovalFlowRecordEntity): ApprovalFlowRecordVO = ApprovalFlowRecordVO(
            id = entity.id,
            scope = entity.scope,
            scopeId = entity.scopeId,
            instanceId = entity.instanceId,
            nodeId = entity.nodeId,
            operatorId = entity.operatorId,
            action = entity.action,
            comment = entity.comment,
            createdTime = entity.createdTime,
        )
    }
}
