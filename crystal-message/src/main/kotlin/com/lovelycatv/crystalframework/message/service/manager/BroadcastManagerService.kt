package com.lovelycatv.crystalframework.message.service.manager

import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerCreateBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerDeleteBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerReadBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerUpdateBroadcastDTO
import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.service.ScopedRelationshipCheckService

/**
 * Management-side CRUD for broadcasts (system announcements / tenant broadcasts). This is a
 * directly-scoped resource: the entity owns `scope_type + scope_id` columns, so the Service
 * supplies [ScopedRelationshipCheckService.resolveRootScope] by reading them directly (the
 * entity is a plain `BaseEntity`, not `BaseScopedEntity`, so the default resolver does not
 * apply). Consumer-side read/mark endpoints live in
 * [com.lovelycatv.crystalframework.message.service.BroadcastService].
 */
interface BroadcastManagerService : CachedBaseManagerService<
        MsgBroadcastRepository,
        MsgBroadcastEntity,
        ManagerCreateBroadcastDTO,
        ManagerReadBroadcastDTO,
        ManagerUpdateBroadcastDTO,
        ManagerDeleteBroadcastDTO
        >, ScopedRelationshipCheckService
