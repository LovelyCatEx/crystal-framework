package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

/**
 * Input for the `POST /manager/storage-provider-routing-rule/simulate` endpoint. All fields are
 * optional — the service fills in reasonable defaults (empty strings, 0L, current timestamp with
 * hourOfDay/dayOfWeek derived from it) so the caller can simulate a "bare minimum upload" without
 * having to enumerate every dimension. Long-range fields are transported as String to stay within
 * the project's Long-as-String convention.
 */
data class SimulateStorageProviderRoutingRuleDTO(
    val userId: String? = null,
    val fileType: Int? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,
    val fileContentType: String? = null,
    val fileSize: String? = null,
    val uploadTimestamp: String? = null,
    val hourOfDay: Int? = null,
    val dayOfWeek: Int? = null,
)
