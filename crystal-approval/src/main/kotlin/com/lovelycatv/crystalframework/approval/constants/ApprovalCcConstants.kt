package com.lovelycatv.crystalframework.approval.constants

/**
 * CC-node notification text constants. Kept in Chinese since the backend does not have
 * an i18n layer — see [com.lovelycatv.crystalframework.approval.service.ApprovalCcNotifier].
 */
object ApprovalCcConstants {
    const val MESSAGE_TITLE_PREFIX = "【审批抄送】"
    const val LINE_FLOW_NAME = "审批流：%s"
    const val LINE_NODE_NAME = "当前节点：%s"
    const val LINE_INITIATOR = "发起人 ID：%s"
    const val LINE_INSTANCE_ID = "流程实例 ID：%s"
    const val LINE_INITIATED_AT = "发起时间：%s"
}
