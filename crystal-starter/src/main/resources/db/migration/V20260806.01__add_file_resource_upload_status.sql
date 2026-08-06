ALTER TABLE public.file_resources
    ADD COLUMN status integer NOT NULL DEFAULT 1,
    ADD COLUMN upload_token character varying(36),
    ADD COLUMN lease_until bigint;

CREATE INDEX idx_file_resources_cleanup
    ON public.file_resources (status, lease_until);

CREATE UNIQUE INDEX uk_file_resources_scope_md5_active
    ON public.file_resources (scope, scope_id, md5)
    WHERE status IN (0, 1);
