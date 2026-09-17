CREATE TABLE IF NOT EXISTS public.economy_transactions (
    id bigint NOT NULL,
    scope integer NOT NULL,
    scope_id bigint NOT NULL,
    owner_id bigint NOT NULL,
    request_id varchar(128) NOT NULL,
    type integer NOT NULL,
    currency_id bigint NOT NULL,
    amount bigint NOT NULL,
    balance_before bigint NOT NULL,
    balance_after bigint NOT NULL,
    reference_type integer NOT NULL DEFAULT 0,
    reference_id bigint,
    remark varchar(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT economy_transactions_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS economy_transactions_request_id_active_uk
    ON public.economy_transactions (request_id) WHERE deleted_time IS NULL;
CREATE INDEX IF NOT EXISTS economy_transactions_scope_idx ON public.economy_transactions (scope, scope_id);
CREATE INDEX IF NOT EXISTS economy_transactions_currency_idx ON public.economy_transactions (currency_id);
