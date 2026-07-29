package com.lovelycatv.crystalframework.test.permission

import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.security.core.GrantedAuthority

/**
 * Fixture record produced by [PermissionMatrixIntegrationTestBase] for one of the four
 * [com.lovelycatv.crystalframework.shared.controller.PermissionMatrix] layers (super / system /
 * tenantAdmin / tenantPem).
 *
 *  - [user] is a real registered [UserEntity] backed by rows in the `users` table (created via
 *    [com.lovelycatv.crystalframework.user.service.UserServiceTest.mockRegisteredUser] so all
 *    RBAC bootstrap side-effects — default `ROLE_USER` grant, event publication — happen too).
 *  - [authentication] is the [UserAuthentication] value the resolver would produce from the JWT
 *    argument-resolver for this user; pass it as the first argument of any controller endpoint.
 *  - [authorities] are the [GrantedAuthority] instances installed into the reactor security
 *    context by [PermissionMatrixIntegrationTestBase.withAuthenticatedUser]. `ROLE_USER` (from
 *    registration) plus the explicitly-granted permissions from the target [layer].
 *  - [layer] records which layer of the matrix the fixture represents so parameterised tests can
 *    label assertions without re-deriving the layer from the permission strings.
 */
data class PermissionMatrixTestUser(
    val user: UserEntity,
    val authentication: UserAuthentication,
    val authorities: Set<GrantedAuthority>,
    val layer: PermissionMatrix.Layer,
)
