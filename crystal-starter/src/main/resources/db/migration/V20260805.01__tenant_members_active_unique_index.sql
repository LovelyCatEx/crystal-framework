-- Enforce at most one active (non-soft-deleted) membership per (tenant, user).
-- Partial index excludes soft-deleted rows (deleted_time IS NOT NULL) so a user who left a tenant
-- can rejoin later, while blocking concurrent duplicate inserts that the application-level
-- "member already exists" check cannot prevent under READ COMMITTED (M-12).
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_members_active
    ON public.tenant_members (tenant_id, member_user_id)
    WHERE deleted_time IS NULL;
