-- Create scoped message_channels table and migrate data from tenant_message_channels.
-- The old tenant-only table is dropped after migration.

-- 1. New scoped table
CREATE TABLE IF NOT EXISTS public.message_channels
(
    id            BIGINT      NOT NULL PRIMARY KEY,
    scope         INT         NOT NULL,
    scope_id      BIGINT      NOT NULL,
    channel_type  INT         NOT NULL,
    name          VARCHAR(64) NOT NULL,
    enabled       BOOLEAN     NOT NULL DEFAULT TRUE,
    config        TEXT        NOT NULL,
    created_time  BIGINT      NOT NULL,
    modified_time BIGINT      NOT NULL,
    deleted_time  BIGINT               DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS message_channels_scope_type_name_unique
    ON public.message_channels USING btree (scope, scope_id, channel_type, name)
    WHERE deleted_time IS NULL;

CREATE INDEX IF NOT EXISTS message_channels_scope_type_idx
    ON public.message_channels USING btree (scope, scope_id, channel_type);

-- 2. Migrate existing tenant-scoped records (scope = 1 corresponds to ResourceScope.TENANT.typeId)
INSERT INTO public.message_channels
    (id, scope, scope_id, channel_type, name, enabled, config, created_time, modified_time, deleted_time)
SELECT id, 1, tenant_id, channel_type, name, enabled, config, created_time, modified_time, deleted_time
FROM public.tenant_message_channels;

-- 3. Drop legacy tenant-only table
DROP TABLE IF EXISTS public.tenant_message_channels;

-- 4. Clean up removed SystemPermission entries. Retained after this migration:
--      MENU_TENANT_MESSAGE_CHANNEL_MANAGER (path=/manager/tenant-message-channels)
--      MENU_SYSTEM_MESSAGE_CHANNEL_MANAGER (path=/manager/system-message-channels)
--      ACTION_MESSAGE_CHANNEL_*         (cross-scope super, name=message.channel.*)
--      ACTION_SYSTEM_MESSAGE_CHANNEL_*  (system scope,      name=system.message.channel.*)
--    Tenant-scope tenantPem declarations live in TenantPermission.kt.
--    RbacTableDataCheckRunner only adds missing rows; orphans left behind are purged here.
DELETE FROM public.user_role_permission_relations
WHERE permission_id IN (
    SELECT id FROM public.user_permissions
    WHERE name IN (
        'tenant.message.channel.create',
        'tenant.message.channel.read',
        'tenant.message.channel.update',
        'tenant.message.channel.delete'
    )
);

DELETE FROM public.user_permissions
WHERE name IN (
    'tenant.message.channel.create',
    'tenant.message.channel.read',
    'tenant.message.channel.update',
    'tenant.message.channel.delete'
);
