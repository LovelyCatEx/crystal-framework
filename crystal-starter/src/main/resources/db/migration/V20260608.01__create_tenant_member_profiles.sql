CREATE TABLE IF NOT EXISTS public.tenant_member_profiles
(
    id               BIGINT       NOT NULL PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL,
    tenant_member_id BIGINT       NOT NULL,
    member_user_id   BIGINT       NOT NULL,

    name             VARCHAR(64)  NOT NULL,
    phone            VARCHAR(32)  NOT NULL,

    nickname         VARCHAR(32),
    avatar           BIGINT,
    email            VARCHAR(256),
    bio              VARCHAR(512),
    gender           INTEGER,
    birthday         BIGINT,
    timezone         VARCHAR(64),
    locale           VARCHAR(16),

    created_time     BIGINT       NOT NULL,
    modified_time    BIGINT       NOT NULL,
    deleted_time     BIGINT                DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS tenant_member_profiles_member_unique_index
    ON public.tenant_member_profiles USING btree (tenant_member_id)
    WHERE deleted_time IS NULL;

CREATE INDEX IF NOT EXISTS tenant_user_profiles_tenant_id_index
    ON public.tenant_member_profiles USING btree (tenant_id);

CREATE INDEX IF NOT EXISTS tenant_user_profiles_tenant_member_user_index
    ON public.tenant_member_profiles USING btree (tenant_id, member_user_id);
