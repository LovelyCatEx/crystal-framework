-- Reset all permission data as part of the permission naming redesign (2026-07-29).
--
-- The 4-layer prefix convention (x. / system. / tenant. / i.tenant.) is now hard-enforced
-- by PermissionMatrix.init. All permission names were renamed in this release to comply,
-- so the previously-registered rows in user_permissions / tenant_permissions no longer
-- match any live Declaration. Every role→permission binding that referenced an old name
-- would be dangling.
--
-- This migration hard-deletes all permissions and their bindings; on next startup
-- SystemSystemRbacConfigurer and TenantBuiltinRbacConfigurer re-register the full permission
-- catalog and re-apply the default role→permission bindings declared in
-- SystemRolePermissionRelation and TenantRolePermissionRelation.
--
-- WARNING: Any user-defined role→permission bindings (custom roles that admins created via
-- the UI) that referenced old permission names are lost. Tenant admins and system admins
-- must re-bind their custom roles after this deployment.
--
-- Note: user_roles / tenant_roles rows themselves are preserved; user_role_relations and
-- tenant_member_role_relations are also preserved (who holds which role does not change).

-- Truncate atomically with CASCADE: PostgreSQL clears the four tables in one statement,
-- releases disk pages immediately (no dead tuples awaiting VACUUM), and CASCADE
-- automatically propagates through the FKs between relations and permissions.
TRUNCATE TABLE
    tenant_role_permission_relations,
    user_role_permission_relations,
    tenant_permissions,
    user_permissions
CASCADE;
