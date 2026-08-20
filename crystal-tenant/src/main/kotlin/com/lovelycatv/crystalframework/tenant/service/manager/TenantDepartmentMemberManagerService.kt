package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import org.springframework.data.relational.core.query.Criteria

interface TenantDepartmentMemberManagerService : BaseTenantResourceManagerService<
        TenantDepartmentMemberRelationRepository,
        TenantDepartmentMemberRelationEntity,
        ManagerCreateTenantDepartmentMemberDTO,
        ManagerReadTenantDepartmentMemberDTO,
        ManagerUpdateTenantDepartmentMemberDTO,
        ManagerDeleteTenantDepartmentMemberDTO
        > {
    override suspend fun buildQueryCriteria(dto: ManagerReadTenantDepartmentMemberDTO): Criteria {
        return super.buildQueryCriteria(
            dto.copy(
                query = GroupNode(
                    logic = QueryLogic.AND,
                    children = listOfNotNull(
                        ConditionNode(
                            field = "department_id",
                            operator = QueryOperator.EQ,
                            value = dto.departmentId,
                        ),
                        dto.query,
                    ),
                ),
            ),
        )
    }

    suspend fun queryVO(dto: ManagerReadTenantDepartmentMemberDTO): PaginatedResponseData<TenantDepartmentMemberVO>
}
