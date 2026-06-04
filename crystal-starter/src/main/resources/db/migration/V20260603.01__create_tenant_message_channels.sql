CREATE TABLE IF NOT EXISTS public.tenant_message_channels
(
    id            BIGINT      NOT NULL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL,
    channel_type  INT         NOT NULL,
    name          VARCHAR(64) NOT NULL,
    enabled       BOOLEAN     NOT NULL DEFAULT TRUE,
    config        TEXT        NOT NULL,
    created_time  BIGINT      NOT NULL,
    modified_time BIGINT      NOT NULL,
    deleted_time  BIGINT               DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS tenant_message_channels_tenant_type_name_unique
    ON public.tenant_message_channels USING btree (tenant_id, channel_type, name)
    WHERE deleted_time IS NULL;

CREATE INDEX IF NOT EXISTS tenant_message_channels_tenant_type_idx
    ON public.tenant_message_channels USING btree (tenant_id, channel_type);
