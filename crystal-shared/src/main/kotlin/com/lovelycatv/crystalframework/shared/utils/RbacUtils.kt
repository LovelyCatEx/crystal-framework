package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.constants.SystemRole
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder

object RbacUtils {
    private const val SPRING_ROLE_PREFIX = "ROLE_"

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
        return hasAllAuthorities(authorities.asList())
    }

    suspend fun hasAllAuthorities(names: Collection<String>): Boolean {
        if (names.isEmpty()) {
            return true
        }
        val userAuthorities = getCurrentAuthorities()
        return names.all { it in userAuthorities }
    }

    /**
     * Whether the current reactive security context has no authenticated principal
     * (e.g. bootstrap `CommandLineRunner` or an `@Unauthorized` endpoint invoked without a token).
     */
    suspend fun isSystemContext(): Boolean {
        val context = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
        return context == null || context.authentication == null
    }

    /**
     * Whether the current principal carries the root role, checking both the raw role name
     * (see [SystemRole.ROLE_ROOT]) and the conventional Spring Security prefixed variant.
     */
    suspend fun isRoot(): Boolean {
        val authorities = getCurrentAuthorities()
        return SystemRole.ROLE_ROOT in authorities ||
            "$SPRING_ROLE_PREFIX${SystemRole.ROLE_ROOT.uppercase()}" in authorities
    }
}