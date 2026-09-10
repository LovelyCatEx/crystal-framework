package com.lovelycatv.crystalframework.ai.constants

import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemRbacPermissionDeclaration

object AiPermission {
    val ACTION_SYSTEM_AI_PROVIDER_CREATE = SystemRbacPermissionDeclaration.action("system.ai.provider.create", "Create AI providers")
    val ACTION_SYSTEM_AI_PROVIDER_READ = SystemRbacPermissionDeclaration.action("system.ai.provider.read", "Read AI providers")
    val ACTION_SYSTEM_AI_PROVIDER_UPDATE = SystemRbacPermissionDeclaration.action("system.ai.provider.update", "Update AI providers")
    val ACTION_SYSTEM_AI_PROVIDER_DELETE = SystemRbacPermissionDeclaration.action("system.ai.provider.delete", "Delete AI providers")

    val ACTION_SYSTEM_AI_MODEL_CREATE = SystemRbacPermissionDeclaration.action("system.ai.model.create", "Create AI models")
    val ACTION_SYSTEM_AI_MODEL_READ = SystemRbacPermissionDeclaration.action("system.ai.model.read", "Read AI models")
    val ACTION_SYSTEM_AI_MODEL_UPDATE = SystemRbacPermissionDeclaration.action("system.ai.model.update", "Update AI models")
    val ACTION_SYSTEM_AI_MODEL_DELETE = SystemRbacPermissionDeclaration.action("system.ai.model.delete", "Delete AI models")

    val ACTION_SYSTEM_AI_USER_GROUP_CREATE = SystemRbacPermissionDeclaration.action("system.ai.user.group.create", "Create AI user groups")
    val ACTION_SYSTEM_AI_USER_GROUP_READ = SystemRbacPermissionDeclaration.action("system.ai.user.group.read", "Read AI user groups")
    val ACTION_SYSTEM_AI_USER_GROUP_UPDATE = SystemRbacPermissionDeclaration.action("system.ai.user.group.update", "Update AI user groups")
    val ACTION_SYSTEM_AI_USER_GROUP_DELETE = SystemRbacPermissionDeclaration.action("system.ai.user.group.delete", "Delete AI user groups")

    val MENU_SYSTEM_AI_PROVIDER = SystemRbacPermissionDeclaration.menu("system.ai.provider", "/manager/ai/provider", "AI Provider management menu")
    val MENU_SYSTEM_AI_MODEL = SystemRbacPermissionDeclaration.menu("system.ai.model", "/manager/ai/model", "AI Model management menu")
    val MENU_SYSTEM_AI_USER_GROUP = SystemRbacPermissionDeclaration.menu("system.ai.user.group", "/manager/ai/user-group", "AI User Group management menu")

    fun allPermissions(): List<SystemRbacPermissionDeclaration> = listOf(
        ACTION_SYSTEM_AI_PROVIDER_CREATE,
        ACTION_SYSTEM_AI_PROVIDER_READ,
        ACTION_SYSTEM_AI_PROVIDER_UPDATE,
        ACTION_SYSTEM_AI_PROVIDER_DELETE,
        ACTION_SYSTEM_AI_MODEL_CREATE,
        ACTION_SYSTEM_AI_MODEL_READ,
        ACTION_SYSTEM_AI_MODEL_UPDATE,
        ACTION_SYSTEM_AI_MODEL_DELETE,
        ACTION_SYSTEM_AI_USER_GROUP_CREATE,
        ACTION_SYSTEM_AI_USER_GROUP_READ,
        ACTION_SYSTEM_AI_USER_GROUP_UPDATE,
        ACTION_SYSTEM_AI_USER_GROUP_DELETE,
        MENU_SYSTEM_AI_PROVIDER,
        MENU_SYSTEM_AI_MODEL,
        MENU_SYSTEM_AI_USER_GROUP,
    )
}
