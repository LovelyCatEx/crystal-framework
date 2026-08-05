package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.sdk.database.TableRegistry
import com.lovelycatv.crystalframework.sdk.database.config.TableConfigurer
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkSQLModifier
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.relational.core.mapping.Table
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
        registry.register(TableConstants.TABLE_MESSAGE_CHANNELS)
        registry.register(TableConstants.TABLE_TENANT_MEMBER_PROFILES)
        registry.register(TableConstants.TABLE_TENANT_DICT_TYPES)
        registry.register(TableConstants.TABLE_TENANT_DICT_ITEMS)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_DEFINITION)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_NODE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_EDGE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_INSTANCE)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_TASK)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_RECORD)
        registry.register(TableConstants.TABLE_APPROVAL_FLOW_TOKEN)
        registry.register(TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES)
        registry.register(TableConstants.TABLE_USER_BAN_RECORDS)

        configurers.forEach { it.configure(registry) }

        val baseEntityTables = registry.getRegistrations()
            .filter { it.isBaseEntity }
            .map { it.tableName }

        verifyAllBaseEntitiesRegistered(baseEntityTables)

        CrystalFrameworkSQLModifier.registerBaseEntityTables(baseEntityTables)
    }

    /**
     * Forward fail-fast guard: every [BaseEntity] subclass carrying a [@Table][Table] annotation
     * in the framework base package must have its physical table registered as a BaseEntity table,
     * otherwise the soft-delete SQL interceptor silently skips it (SELECT keeps returning deleted
     * rows, DELETE physically removes them, modified_time is not maintained).
     *
     * Only the forward direction is checked: a registered table without a matching entity is NOT
     * an error, because external modules register their BaseEntity tables through the
     * [TableConfigurer] SPI and their entity classes live in a separate classloader that this
     * main-package scan cannot see.
     */
    private fun verifyAllBaseEntitiesRegistered(registeredBaseEntityTables: Collection<String>) {
        val registered = registeredBaseEntityTables.map { it.lowercase() }.toSet()

        val scanner = ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(AnnotationTypeFilter(Table::class.java))
        }

        val missing = scanner.findCandidateComponents(GlobalConstants.BASE_PACKAGE)
            .asSequence()
            .filterIsInstance<AnnotatedBeanDefinition>()
            .filter { it.metadata.isAssignableToBaseEntity() }
            .mapNotNull { it.metadata.resolveTableName() }
            .filter { it.lowercase() !in registered }
            .toList()

        if (missing.isNotEmpty()) {
            throw IllegalStateException(
                "BaseEntity tables not registered in TableRegistryInitializer, soft-delete SQL " +
                    "interception would be bypassed for: ${missing.sorted()}. Register them via " +
                    "TableConstants + registry.register(...)."
            )
        }
    }

    private fun AnnotationMetadata.isAssignableToBaseEntity(): Boolean {
        var current: AnnotationMetadata? = this
        val visited = mutableSetOf<String>()
        while (current != null && visited.add(current.className)) {
            if (current.superClassName == BaseEntity::class.java.name) return true
            val superName = current.superClassName ?: return false
            if (superName == Any::class.java.name) return false
            current = runCatching {
                SimpleMetadataReaderFactory().getMetadataReader(superName).annotationMetadata
            }.getOrNull()
        }
        return false
    }

    private fun AnnotationMetadata.resolveTableName(): String? {
        val attrs = getAnnotationAttributes(Table::class.java.name) ?: return null
        val value = attrs["value"] as? String
        val name = attrs["name"] as? String
        return listOf(value, name).firstOrNull { !it.isNullOrBlank() }
    }
}
