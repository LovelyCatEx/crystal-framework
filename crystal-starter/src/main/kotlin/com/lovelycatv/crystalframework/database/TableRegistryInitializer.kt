package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.sdk.database.TableRegistry
import com.lovelycatv.crystalframework.sdk.database.config.TableConfigurer
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkSQLModifier
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TableRegistryInitializer(
    configurers: List<TableConfigurer> = emptyList(),
) {

    init {
        val registry = TableRegistry()

        registry.register(TableConstants.TABLE_USERS)
        registry.register(TableConstants.TABLE_TENANTS)
        registry.register(TableConstants.TABLE_TENANT_ROLES)
        registry.register(TableConstants.TABLE_TENANT_PERMISSIONS)
        registry.register(TableConstants.TABLE_TENANT_ROLE_PERMISSION_RELATIONS)
        registry.register(TableConstants.TABLE_TENANT_MEMBERS)
        registry.register(TableConstants.TABLE_TENANT_MEMBER_ROLE_RELATIONS)
        registry.register(TableConstants.TABLE_TENANT_DEPARTMENTS)
        registry.register(TableConstants.TABLE_TENANT_DEPARTMENT_MEMBER_RELATIONS)
        registry.register(TableConstants.TABLE_TENANT_INVITATIONS)
        registry.register(TableConstants.TABLE_TENANT_INVITATION_RECORDS)
        registry.register(TableConstants.TABLE_TENANT_TIRE_TYPES)
        registry.register(TableConstants.TABLE_USER_ROLES)
        registry.register(TableConstants.TABLE_USER_ROLE_RELATIONS)
        registry.register(TableConstants.TABLE_USER_ROLE_PERMISSION_RELATIONS)
        registry.register(TableConstants.TABLE_USER_PERMISSIONS)
        registry.register(TableConstants.TABLE_OAUTH_ACCOUNTS)
        registry.register(TableConstants.TABLE_SYSTEM_SETTINGS)
        registry.register(TableConstants.TABLE_AUDIT_MANAGER_LOGS)
        registry.register(TableConstants.TABLE_MAIL_SEND_LOGS)
        registry.register(TableConstants.TABLE_MAIL_TEMPLATE_CATEGORIES)
        registry.register(TableConstants.TABLE_MAIL_TEMPLATE_TYPES)
        registry.register(TableConstants.TABLE_MAIL_TEMPLATES)
        registry.register(TableConstants.TABLE_USER_LOGIN_LOGS)
        registry.register(TableConstants.TABLE_FILE_RESOURCES)
        registry.register(TableConstants.TABLE_STORAGE_PROVIDERS)
        registry.register(TableConstants.TABLE_SYSTEM_ANNOUNCEMENTS)
        registry.register(TableConstants.TABLE_TENANT_TIRE_BENEFIT_FEATURES)
        registry.register(TableConstants.TABLE_TENANT_TIRE_BENEFIT_VALUES)
        registry.register(TableConstants.TABLE_TENANT_SETTINGS)
        registry.register(TableConstants.TABLE_TENANT_MESSAGE_CHANNELS)
        registry.register(TableConstants.TABLE_TENANT_USER_PROFILES)
        registry.register(TableConstants.TABLE_TENANT_DICT_TYPES)
        registry.register(TableConstants.TABLE_TENANT_DICT_ITEMS)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_DEFINITION)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_NODE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_EDGE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_INSTANCE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_TASK)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_RECORD)

        configurers.forEach { it.configure(registry) }

        val baseEntityTables = registry.getRegistrations()
            .filter { it.isBaseEntity }
            .map { it.tableName }

        CrystalFrameworkSQLModifier.registerBaseEntityTables(baseEntityTables)
    }
}
