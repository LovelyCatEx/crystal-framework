package com.lovelycatv.crystalframework.audit.controller.manager.auditlog

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAuditLogDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
