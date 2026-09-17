CREATE TABLE IF NOT EXISTS public.economy_currencies (
    id bigint NOT NULL,
    code varchar(32) NOT NULL,
    name varchar(64) NOT NULL,
    symbol varchar(16) NOT NULL,
    precision integer NOT NULL DEFAULT 2,
    symbol_position integer NOT NULL DEFAULT 0,
    decimal_separator varchar(8) NOT NULL DEFAULT '.',
    thousands_separator varchar(8) NOT NULL DEFAULT ',',
    description varchar(512),
    enabled boolean NOT NULL DEFAULT true,
    sort integer NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    CONSTRAINT economy_currencies_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS economy_currencies_code_active_uk
    ON public.economy_currencies (code) WHERE deleted_time IS NULL;
