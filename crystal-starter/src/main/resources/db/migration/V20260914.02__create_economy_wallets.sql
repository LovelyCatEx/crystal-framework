CREATE TABLE IF NOT EXISTS public.economy_wallets (
    id bigint NOT NULL,
    scope integer NOT NULL,
    scope_id bigint NOT NULL,
    owner_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    balance bigint NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT economy_wallets_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS economy_wallets_scope_owner_currency_active_uk
    ON public.economy_wallets (scope, scope_id, owner_id, currency_id) WHERE deleted_time IS NULL;
CREATE INDEX IF NOT EXISTS economy_wallets_scope_idx ON public.economy_wallets (scope, scope_id);
CREATE INDEX IF NOT EXISTS economy_wallets_currency_idx ON public.economy_wallets (currency_id);
