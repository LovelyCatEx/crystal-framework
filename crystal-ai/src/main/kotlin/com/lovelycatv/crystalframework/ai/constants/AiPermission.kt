package com.lovelycatv.crystalframework.ai.constants

import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemRbacPermissionDeclaration

object AiPermission {
    const val ACTION_SYSTEM_AI_PROVIDER_CREATE_NAME = "system.ai.provider.create"
    val ACTION_SYSTEM_AI_PROVIDER_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_PROVIDER_CREATE_NAME, "Create AI providers")

    const val ACTION_SYSTEM_AI_PROVIDER_READ_NAME = "system.ai.provider.read"
    val ACTION_SYSTEM_AI_PROVIDER_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_PROVIDER_READ_NAME, "Read AI providers")

    const val ACTION_SYSTEM_AI_PROVIDER_UPDATE_NAME = "system.ai.provider.update"
    val ACTION_SYSTEM_AI_PROVIDER_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_PROVIDER_UPDATE_NAME, "Update AI providers")

    const val ACTION_SYSTEM_AI_PROVIDER_DELETE_NAME = "system.ai.provider.delete"
    val ACTION_SYSTEM_AI_PROVIDER_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_PROVIDER_DELETE_NAME, "Delete AI providers")

    const val ACTION_SYSTEM_AI_MODEL_CREATE_NAME = "system.ai.model.create"
    val ACTION_SYSTEM_AI_MODEL_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_MODEL_CREATE_NAME, "Create AI models")

    const val ACTION_SYSTEM_AI_MODEL_READ_NAME = "system.ai.model.read"
    val ACTION_SYSTEM_AI_MODEL_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_MODEL_READ_NAME, "Read AI models")

    const val ACTION_SYSTEM_AI_MODEL_UPDATE_NAME = "system.ai.model.update"
    val ACTION_SYSTEM_AI_MODEL_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_MODEL_UPDATE_NAME, "Update AI models")

    const val ACTION_SYSTEM_AI_MODEL_DELETE_NAME = "system.ai.model.delete"
    val ACTION_SYSTEM_AI_MODEL_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_MODEL_DELETE_NAME, "Delete AI models")

    const val ACTION_SYSTEM_AI_USER_GROUP_CREATE_NAME = "system.ai.user.group.create"
    val ACTION_SYSTEM_AI_USER_GROUP_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_CREATE_NAME, "Create AI user groups")

    const val ACTION_SYSTEM_AI_USER_GROUP_READ_NAME = "system.ai.user.group.read"
    val ACTION_SYSTEM_AI_USER_GROUP_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_READ_NAME, "Read AI user groups")

    const val ACTION_SYSTEM_AI_USER_GROUP_UPDATE_NAME = "system.ai.user.group.update"
    val ACTION_SYSTEM_AI_USER_GROUP_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_UPDATE_NAME, "Update AI user groups")

    const val ACTION_SYSTEM_AI_USER_GROUP_DELETE_NAME = "system.ai.user.group.delete"
    val ACTION_SYSTEM_AI_USER_GROUP_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_DELETE_NAME, "Delete AI user groups")

    const val ACTION_SYSTEM_AI_USER_GROUP_MODEL_CREATE_NAME = "system.ai.user.group.model.create"
    val ACTION_SYSTEM_AI_USER_GROUP_MODEL_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MODEL_CREATE_NAME, "Create AI user group model associations")

    const val ACTION_SYSTEM_AI_USER_GROUP_MODEL_READ_NAME = "system.ai.user.group.model.read"
    val ACTION_SYSTEM_AI_USER_GROUP_MODEL_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MODEL_READ_NAME, "Read AI user group model associations")

    // const val ACTION_SYSTEM_AI_USER_GROUP_MODEL_UPDATE_NAME = "system.ai.user.group.model.update"
    // val ACTION_SYSTEM_AI_USER_GROUP_MODEL_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MODEL_UPDATE_NAME, "Update AI user group model associations")

    const val ACTION_SYSTEM_AI_USER_GROUP_MODEL_DELETE_NAME = "system.ai.user.group.model.delete"
    val ACTION_SYSTEM_AI_USER_GROUP_MODEL_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MODEL_DELETE_NAME, "Delete AI user group model associations")

    const val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_CREATE_NAME = "system.ai.user.group.member.create"
    val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MEMBER_CREATE_NAME, "Create AI user group member associations")

    const val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_READ_NAME = "system.ai.user.group.member.read"
    val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MEMBER_READ_NAME, "Read AI user group member associations")

    // const val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_UPDATE_NAME = "system.ai.user.group.member.update"
    // val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MEMBER_UPDATE_NAME, "Update AI user group member associations")

    const val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_DELETE_NAME = "system.ai.user.group.member.delete"
    val ACTION_SYSTEM_AI_USER_GROUP_MEMBER_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_AI_USER_GROUP_MEMBER_DELETE_NAME, "Delete AI user group member associations")

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
        ACTION_SYSTEM_AI_USER_GROUP_MODEL_CREATE,
        ACTION_SYSTEM_AI_USER_GROUP_MODEL_READ,
        // ACTION_SYSTEM_AI_USER_GROUP_MODEL_UPDATE,
        ACTION_SYSTEM_AI_USER_GROUP_MODEL_DELETE,
        ACTION_SYSTEM_AI_USER_GROUP_MEMBER_CREATE,
        ACTION_SYSTEM_AI_USER_GROUP_MEMBER_READ,
        // ACTION_SYSTEM_AI_USER_GROUP_MEMBER_UPDATE,
        ACTION_SYSTEM_AI_USER_GROUP_MEMBER_DELETE,
        MENU_SYSTEM_AI_PROVIDER,
        MENU_SYSTEM_AI_MODEL,
        MENU_SYSTEM_AI_USER_GROUP,
    )
}
