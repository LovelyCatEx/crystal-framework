package com.lovelycatv.crystalframework.audit.types

enum class SessionType(val typeId: Int) {
    /**
     * Interactive user session — created by CustomAuthFilter on a successful login;
     * carries `AUDIT_USER_ID` (and optionally `AUDIT_TENANT_ID`) attributes.
     */
    USER(0),

    /**
     * System-generated session used by Prometheus / actuator scrape endpoints. These
     * requests never hit CustomAuthFilter and therefore have no `AUDIT_USER_ID` attribute.
     */
    PROMETHEUS(1);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
