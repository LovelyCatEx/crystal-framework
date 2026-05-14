-- Audit manager logs table for tracking manager CRUD operations

CREATE TABLE IF NOT EXISTS public.audit_manager_logs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    username character varying(64) NOT NULL,
    tenant_id bigint,
    action integer NOT NULL,
    resource_type character varying(128) NOT NULL,
    resource_ids text,
    request_id bigint,
    http_method character varying(16),
    path character varying(512),
    remote_ip character varying(64),
    user_agent character varying(512),
    success boolean NOT NULL,
    error_message text,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

ALTER TABLE public.audit_manager_logs ADD CONSTRAINT audit_manager_logs_pk PRIMARY KEY (id);

CREATE INDEX audit_manager_logs_user_id_index ON public.audit_manager_logs USING btree (user_id);
CREATE INDEX audit_manager_logs_action_index ON public.audit_manager_logs USING btree (action);
CREATE INDEX audit_manager_logs_resource_type_index ON public.audit_manager_logs USING btree (resource_type);
CREATE INDEX audit_manager_logs_created_time_index ON public.audit_manager_logs USING btree (created_time);
