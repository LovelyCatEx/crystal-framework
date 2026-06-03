CREATE TABLE IF NOT EXISTS public.tenant_settings
(
    id            BIGINT               NOT NULL PRIMARY KEY,
    tenant_id     BIGINT               NOT NULL,
    config_key    VARCHAR(256)         NOT NULL,
    config_value  TEXT,
    created_time  BIGINT               NOT NULL,
    modified_time BIGINT               NOT NULL,
    deleted_time  BIGINT                        DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS tenant_settings_tenant_key_unique_index
    ON public.tenant_settings USING btree (tenant_id, config_key);

CREATE INDEX IF NOT EXISTS tenant_settings_tenant_id_index
    ON public.tenant_settings USING btree (tenant_id);
