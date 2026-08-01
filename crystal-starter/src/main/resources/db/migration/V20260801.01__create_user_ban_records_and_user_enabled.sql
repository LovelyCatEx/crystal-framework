-- Add enabled flag to users and create user_ban_records table

ALTER TABLE public.users ADD COLUMN enabled boolean NOT NULL DEFAULT true;

CREATE TABLE IF NOT EXISTS public.user_ban_records (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    reason character varying(512) NOT NULL,
    ban_until bigint,
    lifted_time bigint,
    operator_user_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

ALTER TABLE public.user_ban_records ADD CONSTRAINT user_ban_records_pk PRIMARY KEY (id);

CREATE INDEX user_ban_records_user_id_index ON public.user_ban_records USING btree (user_id);
