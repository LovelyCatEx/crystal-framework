package com.lovelycatv.crystalframework.message.controller.dto

import jakarta.validation.constraints.NotBlank

/**
 * Payload for sending a point-to-point message to another user. [targetUserId] is a
 * Long serialized as String (per the framework's Long-as-String rule); [contentType]
 * is a [com.lovelycatv.crystalframework.message.types.ContentType] typeId, defaulting
 * to TEXT when null.
 */
data class SendMessageDTO(
    @field:NotBlank(message = "targetUserId must not be blank")
    val targetUserId: String = "",
    @field:NotBlank(message = "content must not be blank")
    val content: String = "",
    val contentType: Int? = null,
)
