package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository

interface UserManagerService : CachedBaseManagerService<
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
> {
    /**
     * Query users using the unified [QueryNode] path.
     *
     * If [dto.query] is already provided by the caller, it is passed through as-is.
     *
     * Otherwise, any legacy filter fields (username, email, nickname, searchKeyword,
     * startTime, endTime) are translated into a [GroupNode] tree automatically, so
     * the same [R2dbcEntityTemplate] code path handles all queries.
     *
     * The old [UserRepository.advanceSearch] SQL queries are no longer called.
     */
    override suspend fun query(
        dto: ManagerReadUserDTO,
        isAdvanceQuery: suspend (dto: ManagerReadUserDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadUserDTO, limit: Int, offset: Int) -> PaginatedResponseData<UserEntity>
    ): PaginatedResponseData<UserEntity> {
        // Caller already provided an explicit query tree — pass through directly.
        if (dto.query != null) {
            return super.query(dto, isAdvanceQuery, doAdvanceQuery)
        }

        // Translate legacy DTO fields into a QueryNode tree.
        val nodes = buildList<QueryNode> {
            // searchKeyword: match username OR email OR nickname
            dto.searchKeyword?.let { kw ->
                add(GroupNode(
                    logic = QueryLogic.OR,
                    children = listOf(
                        ConditionNode(field = "username",  operator = QueryOperator.CONTAINS, value = kw),
                        ConditionNode(field = "email",     operator = QueryOperator.CONTAINS, value = kw),
                        ConditionNode(field = "nickname",  operator = QueryOperator.CONTAINS, value = kw),
                    )
                ))
            }
            dto.username?.let  { add(ConditionNode(field = "username",      operator = QueryOperator.CONTAINS, value = it)) }
            dto.email?.let     { add(ConditionNode(field = "email",         operator = QueryOperator.CONTAINS, value = it)) }
            dto.nickname?.let  { add(ConditionNode(field = "nickname",      operator = QueryOperator.CONTAINS, value = it)) }
            dto.startTime?.let { add(ConditionNode(field = "created_time",  operator = QueryOperator.GTE,      value = it)) }
            dto.endTime?.let   { add(ConditionNode(field = "created_time",  operator = QueryOperator.LTE,      value = it)) }
        }

        if (nodes.isEmpty()) {
            // No filters — simple pagination
            return super.query(dto, isAdvanceQuery, doAdvanceQuery)
        }

        val queryNode: QueryNode = if (nodes.size == 1) nodes.first()
                                   else GroupNode(logic = QueryLogic.AND, children = nodes)

        return super.query(dto.copy(query = queryNode), isAdvanceQuery, doAdvanceQuery)
    }
}
