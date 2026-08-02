package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.service.ApprovalDictResolver
import com.lovelycatv.crystalframework.approval.types.ApprovalFieldOption
import com.lovelycatv.crystalframework.approval.types.ApprovalFieldType
import com.lovelycatv.crystalframework.approval.types.ApprovalFormSchema
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.parseObject
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictItemManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import org.springframework.stereotype.Service

@Service
class ApprovalDictResolverImpl(
    private val tenantDictTypeManagerService: TenantDictTypeManagerService,
    private val tenantDictItemManagerService: TenantDictItemManagerService,
) : ApprovalDictResolver {

    override suspend fun resolveItems(
        flowScope: ResourceScope,
        flowScopeId: Long,
        dictCode: String,
        dictScope: Int?,
    ): List<ApprovalFieldOption> {
        val (scope, scopeId) = resolveScopeCoordinate(flowScope, flowScopeId, dictScope)
        val dictType = tenantDictTypeManagerService.findByScopeAndCode(scope, scopeId, dictCode)
            ?: return emptyList()
        return tenantDictItemManagerService.findEnabledByTypeId(dictType.id)
            .map { ApprovalFieldOption(value = it.itemCode, label = it.itemValue) }
    }

    override suspend fun resolveItemsForField(
        schemaJson: String?,
        fieldKey: String,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): List<ApprovalFieldOption> {
        if (schemaJson.isNullOrBlank()) return emptyList()
        val schema = runCatching { schemaJson.parseObject<ApprovalFormSchema>() }.getOrNull() ?: return emptyList()
        val field = schema.fields.firstOrNull { it.key == fieldKey } ?: return emptyList()
        if (field.type != ApprovalFieldType.DICT || field.dictCode.isNullOrBlank()) return emptyList()
        return resolveItems(flowScope, flowScopeId, field.dictCode, field.dictScope)
    }

    override suspend fun resolveAllowedCodes(
        schema: ApprovalFormSchema,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): Map<String, Set<String>> {
        return schema.fields
            .filter { it.type == ApprovalFieldType.DICT && !it.dictCode.isNullOrBlank() }
            .associate { field ->
                val codes = resolveItems(flowScope, flowScopeId, field.dictCode!!, field.dictScope)
                    .map { it.value }
                    .toSet()
                field.key to codes
            }
    }

    override suspend fun validateDictBindings(
        schema: ApprovalFormSchema,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): List<String> {
        val errors = mutableListOf<String>()
        for (field in schema.fields) {
            if (field.type != ApprovalFieldType.DICT) continue
            val dictCode = field.dictCode
            if (dictCode.isNullOrBlank()) continue // structural check already reported by the pure validator

            val coordinate = runCatching { resolveScopeCoordinate(flowScope, flowScopeId, field.dictScope) }
                .getOrElse {
                    errors += "Field '${field.key}': ${it.message}"
                    continue
                }
            val (scope, scopeId) = coordinate
            if (tenantDictTypeManagerService.findByScopeAndCode(scope, scopeId, dictCode) == null) {
                errors += "Field '${field.key}' references dict code '$dictCode' " +
                    "not found in scope ${scope.name} ($scopeId)"
            }
        }
        return errors
    }

    /**
     * Single source of truth for the DICT scope model. Exhaustive over [ResourceScope] with no
     * `else` — every scope pairing is either mapped or explicitly rejected with a descriptive
     * [BusinessException].
     */
    private fun resolveScopeCoordinate(
        flowScope: ResourceScope,
        flowScopeId: Long,
        dictScope: Int?,
    ): Pair<ResourceScope, Long> {
        val override = dictScope?.let {
            ResourceScope.getById(it) ?: throw BusinessException("dictScope $it is not a valid ResourceScope")
        }
        val effective = override ?: flowScope
        return when (effective) {
            ResourceScope.SYSTEM -> ResourceScope.SYSTEM to SYSTEM_SCOPE_ID
            ResourceScope.TENANT -> resolveTenantCoordinate(flowScope, flowScopeId)
        }
    }

    /**
     * A TENANT-scoped dict lookup is only legal when the owning flow is itself TENANT-scoped —
     * a SYSTEM flow reaching into a tenant's private dictionary is rejected. Exhaustive, no `else`.
     */
    private fun resolveTenantCoordinate(
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): Pair<ResourceScope, Long> = when (flowScope) {
        ResourceScope.TENANT -> ResourceScope.TENANT to flowScopeId
        ResourceScope.SYSTEM -> throw BusinessException("A SYSTEM-scope flow cannot reference a TENANT-scope dict")
    }

    companion object {
        /** System-scope resources share a single global scope id of 0 (see BaseScopedEntity default). */
        private const val SYSTEM_SCOPE_ID = 0L
    }
}
