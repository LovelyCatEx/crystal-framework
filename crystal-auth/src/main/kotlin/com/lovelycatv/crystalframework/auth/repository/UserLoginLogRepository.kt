package com.lovelycatv.crystalframework.auth.repository

import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface UserLoginLogRepository : BaseRepository<UserLoginLogEntity>
