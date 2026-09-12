CREATE TABLE IF NOT EXISTS public.ai_providers (
    id bigint NOT NULL,
    name varchar(64) NOT NULL,
    key varchar(128) NOT NULL,
    description varchar(512),
    protocol_type integer NOT NULL,
    base_url varchar(512) NOT NULL,
    api_key text NOT NULL,
    chat_completions_path varchar(256),
    embedding_path varchar(256),
    request_config text NOT NULL,
    response_config text NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    sort integer NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT ai_providers_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS ai_providers_key_active_uk
    ON public.ai_providers (key) WHERE deleted_time IS NULL;

CREATE TABLE IF NOT EXISTS public.ai_models (
    id bigint NOT NULL,
    provider_id bigint NOT NULL,
    key varchar(128) NOT NULL,
    model_name varchar(256) NOT NULL,
    display_name varchar(128) NOT NULL,
    description varchar(512),
    capabilities text NOT NULL,
    context_window_tokens bigint NOT NULL,
    max_output_tokens bigint,
    input_price_per_million numeric(19,6) NOT NULL,
    output_price_per_million numeric(19,6) NOT NULL,
    cache_read_price_per_million numeric(19,6),
    cache_write_price_per_million numeric(19,6),
    currency varchar(16) NOT NULL,
    request_config text NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    sort integer NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT ai_models_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS ai_models_provider_key_active_uk
    ON public.ai_models (provider_id, key) WHERE deleted_time IS NULL;
CREATE INDEX IF NOT EXISTS ai_models_provider_idx ON public.ai_models (provider_id);

CREATE TABLE IF NOT EXISTS public.ai_user_groups (
    id bigint NOT NULL,
    name varchar(128) NOT NULL,
    key varchar(128) NOT NULL,
    description varchar(512),
    billing_multiplier numeric(19,6) NOT NULL DEFAULT 1.000000,
    enabled boolean NOT NULL DEFAULT true,
    is_default boolean NOT NULL DEFAULT false,
    sort integer NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT ai_user_groups_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS ai_user_groups_key_active_uk
    ON public.ai_user_groups (key) WHERE deleted_time IS NULL;

CREATE TABLE IF NOT EXISTS public.ai_user_group_members (
    id bigint NOT NULL,
    user_group_id bigint NOT NULL,
    user_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT ai_user_group_members_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS ai_user_group_members_active_uk
    ON public.ai_user_group_members (user_group_id, user_id) WHERE deleted_time IS NULL;
CREATE INDEX IF NOT EXISTS ai_user_group_members_user_idx ON public.ai_user_group_members (user_id);

CREATE TABLE IF NOT EXISTS public.ai_user_group_models (
    id bigint NOT NULL,
    user_group_id bigint NOT NULL,
    model_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT ai_user_group_models_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS ai_user_group_models_active_uk
    ON public.ai_user_group_models (user_group_id, model_id) WHERE deleted_time IS NULL;
CREATE INDEX IF NOT EXISTS ai_user_group_models_model_idx ON public.ai_user_group_models (model_id);
