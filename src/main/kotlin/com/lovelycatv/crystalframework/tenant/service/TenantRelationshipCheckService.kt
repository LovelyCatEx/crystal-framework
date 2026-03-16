package com.lovelycatv.crystalframework.tenant.service

interface TenantRelationshipCheckService {
    /**
     * Checks if a single entity is related to the specified tenant
     *
     * @param id The entity ID to check
     * @param tenantId The tenant ID to verify relationship against
     * @return true if the entity belongs to the tenant, false otherwise
     */
    suspend fun checkIsRelated(id: Long, tenantId: Long): Boolean {
        return this.checkIsRelated(listOf(id), tenantId)
    }

    /**
     * Checks if multiple entities are related to the specified tenant
     *
     * @param ids Collection of entity IDs to check
     * @param tenantId The tenant ID to verify relationships against
     * @return true if all entities belong to the tenant, false if any entity is not related
     */
    suspend fun checkIsRelated(ids: Collection<Long>, tenantId: Long): Boolean
}