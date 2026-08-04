package com.lovelycatv.crystalframework.approval.constants

/**
 * Keys used inside an APPROVAL node's [com.lovelycatv.crystalframework.approval.types.ApprovalNodeConfig.strategyParams].
 *
 * For the SPECIFIED_USER strategy the approver identifiers live under one of these keys depending on
 * the flow scope: TENANT flows carry tenant_members primary keys under [STRATEGY_PARAM_MEMBER_IDS],
 * SYSTEM flows carry user ids under [STRATEGY_PARAM_USER_IDS].
 */
object ApprovalNodeConfigConstants {
    const val STRATEGY_PARAM_MEMBER_IDS = "memberIds"
    const val STRATEGY_PARAM_USER_IDS = "userIds"
}
