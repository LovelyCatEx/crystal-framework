package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration
import java.time.Instant
import java.time.ZoneId

/**
 * Everything a [StorageProviderRouter] can know about an upload at routing time, exposed as a
 * flat map via [toEvaluationMap] so `QueryNodeEvaluator` can match routing rule condition trees
 * against it. v1 scope only — tenantId/role are deliberately left out (see plan), add later by
 * extending this data class and [of].
 */
data class RoutingContext(
    val userId: Long,
    val fileType: ResourceFileTypeDeclaration,
    val fileName: String,
    val fileExtension: String,
    val fileContentType: String,
    val fileSize: Long,
    val uploadTimestamp: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int,
) {
    fun toEvaluationMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "fileType" to fileType.typeId,
        "fileName" to fileName,
        "fileExtension" to fileExtension,
        "fileContentType" to fileContentType,
        "fileSize" to fileSize,
        "uploadTimestamp" to uploadTimestamp,
        "hourOfDay" to hourOfDay,
        "dayOfWeek" to dayOfWeek,
    )

    companion object {
        fun of(
            userId: Long,
            fileType: ResourceFileTypeDeclaration,
            fileName: String,
            fileContentType: String = "application/octet-stream",
            fileSize: Long = -1L,
            uploadTimestamp: Long = System.currentTimeMillis(),
        ): RoutingContext {
            val zoned = Instant.ofEpochMilli(uploadTimestamp).atZone(ZoneId.systemDefault())
            return RoutingContext(
                userId = userId,
                fileType = fileType,
                fileName = fileName,
                fileExtension = fileName.substringAfterLast('.', ""),
                fileContentType = fileContentType,
                fileSize = fileSize,
                uploadTimestamp = uploadTimestamp,
                hourOfDay = zoned.hour,
                dayOfWeek = zoned.dayOfWeek.value,
            )
        }
    }
}
