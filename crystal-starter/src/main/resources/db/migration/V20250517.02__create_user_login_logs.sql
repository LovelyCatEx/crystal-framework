-- User login logs table for tracking user login operations

CREATE TABLE IF NOT EXISTS public.user_login_logs (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(64),
    tenant_id bigint,
    login_method smallint NOT NULL,
    oauth2_type smallint,
    oauth2_username character varying(128),
    oauth2_account_id bigint,
    success boolean NOT NULL,
    error_message character varying(512),
    remote_ip character varying(64),
    user_agent character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

ALTER TABLE public.user_login_logs ADD CONSTRAINT user_login_logs_pk PRIMARY KEY (id);

CREATE INDEX user_login_logs_user_id_index ON public.user_login_logs USING btree (user_id);
CREATE INDEX user_login_logs_username_index ON public.user_login_logs USING btree (username);
CREATE INDEX user_login_logs_tenant_id_index ON public.user_login_logs USING btree (tenant_id);
CREATE INDEX user_login_logs_login_method_index ON public.user_login_logs USING btree (login_method);
CREATE INDEX user_login_logs_oauth2_type_index ON public.user_login_logs USING btree (oauth2_type);
CREATE INDEX user_login_logs_success_index ON public.user_login_logs USING btree (success);
CREATE INDEX user_login_logs_remote_ip_index ON public.user_login_logs USING btree (remote_ip);
CREATE INDEX user_login_logs_created_time_index ON public.user_login_logs USING btree (created_time);