-- Add scope + tenant_id to oauth_accounts so a single third-party identity can be bound
-- at the system level and/or within individual tenants.
--
-- scope: 0 = SYSTEM, 1 = TENANT (see OAuthBindingScope).
-- tenant_id: required when scope = TENANT, NULL when scope = SYSTEM.
--
-- A given (platform, identifier) may have at most one row per (scope, tenant_id), i.e. one
-- system-level binding and at most one binding per tenant. The cross-row invariant "all
-- non-null user_id for the same (platform, identifier) belong to the same user" is enforced
-- at the application layer (cannot be expressed as a column constraint).

ALTER TABLE public.oauth_accounts
    ADD COLUMN IF NOT EXISTS scope INTEGER NOT NULL DEFAULT 0;

ALTER TABLE public.oauth_accounts
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT NULL;

-- Existing rows are system-level bindings (scope already defaults to 0, tenant_id NULL).

-- One binding per (platform, identifier, scope, tenant). COALESCE folds the NULL tenant_id of
-- SYSTEM rows into a sentinel so they participate in uniqueness. Soft-deleted rows are excluded
-- so an unbind (soft delete) frees the slot for a future re-bind.
CREATE UNIQUE INDEX IF NOT EXISTS oauth_accounts_platform_identifier_scope_tenant_unique_index
    ON public.oauth_accounts USING btree (platform, identifier, scope, COALESCE(tenant_id, 0))
    WHERE deleted_time IS NULL;

CREATE INDEX IF NOT EXISTS oauth_accounts_tenant_id_index
    ON public.oauth_accounts USING btree (tenant_id);
