package com.lovelycatv.crystalframework.shared.exception

/**
 * Structured payload attached to the 403 [com.lovelycatv.crystalframework.shared.response.ApiResponse]
 * returned when a login is rejected because the account has been disabled by an administrator
 * (Spring Security's [org.springframework.security.authentication.DisabledException]).
 *
 * Unlike [BanContext] a disabled account carries no dynamic data (no reason / expiry), so this is a
 * pure marker whose `disabled` flag lets the frontend tell it apart from a ban or a generic forbidden
 * response and render the dedicated "account disabled" dialog.
 */
data class DisabledContext(
    val disabled: Boolean = true,
)
