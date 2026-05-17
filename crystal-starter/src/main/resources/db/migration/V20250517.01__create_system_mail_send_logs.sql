-- Mail send logs table for tracking mail sending operations

CREATE TABLE IF NOT EXISTS public.mail_send_logs (
    id bigint NOT NULL,
    from_email character varying(256) NOT NULL,
    to_email character varying(256) NOT NULL,
    subject character varying(512) NOT NULL,
    content text NOT NULL,
    success boolean NOT NULL,
    error_message text,
    user_id bigint,
    tenant_id bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

ALTER TABLE public.mail_send_logs ADD CONSTRAINT mail_send_logs_pk PRIMARY KEY (id);

CREATE INDEX mail_send_logs_to_email_index ON public.mail_send_logs USING btree (to_email);
CREATE INDEX mail_send_logs_success_index ON public.mail_send_logs USING btree (success);
CREATE INDEX mail_send_logs_created_time_index ON public.mail_send_logs USING btree (created_time);
CREATE INDEX mail_send_logs_user_id_index ON public.mail_send_logs USING btree (user_id);
CREATE INDEX mail_send_logs_tenant_id_index ON public.mail_send_logs USING btree (tenant_id);