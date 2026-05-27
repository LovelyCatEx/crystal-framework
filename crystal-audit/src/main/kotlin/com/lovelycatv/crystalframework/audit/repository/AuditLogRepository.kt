package com.lovelycatv.crystalframework.audit.repository

import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : BaseRepository<AuditLogEntity>
