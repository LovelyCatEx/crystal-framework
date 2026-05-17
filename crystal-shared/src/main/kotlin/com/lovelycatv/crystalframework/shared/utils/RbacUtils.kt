package com.lovelycatv.crystalframework.shared.utils

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder

object RbacUtils {
    suspend fun getCurrentAuthorities(): List<String> {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication }
            .flatMapIterable { it.authorities }
            .mapNotNull { it.authority }
            .collectList()
            .awaitFirstOrNull()
            ?.filterNotNull()
            ?: emptyList()
    }

    suspend fun hasAuthority(authority: String): Boolean {
        return getCurrentAuthorities().contains(authority)
    }

    suspend fun hasAnyAuthority(vararg authorities: String): Boolean {
        val userAuthorities = getCurrentAuthorities()
        return authorities.any { it in userAuthorities }
    }

    suspend fun hasAllAuthorities(vararg authorities: String): Boolean {
        val userAuthorities = getCurrentAuthorities()
        return authorities.all { it in userAuthorities }
    }
}