-- Fix the unique index to exclude soft-deleted rows
-- Without deleted_time IS NULL, soft-deleted UPLOADING/COMMITTED rows would still block re-uploads

DROP INDEX IF EXISTS uk_file_resources_scope_md5_active;

CREATE UNIQUE INDEX uk_file_resources_scope_md5_active
    ON public.file_resources (scope, scope_id, md5)
    WHERE deleted_time IS NULL AND status IN (0, 1);
