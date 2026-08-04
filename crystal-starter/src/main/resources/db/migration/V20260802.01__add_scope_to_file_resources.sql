-- Upgrade file_resources to the scope + scope_id pattern (BaseScopedEntity).
-- Existing rows are backfilled as SYSTEM scope (scope = 0, scope_id = 0).
ALTER TABLE public.file_resources ADD COLUMN scope integer NOT NULL DEFAULT 0;
ALTER TABLE public.file_resources ADD COLUMN scope_id bigint NOT NULL DEFAULT 0;

CREATE INDEX idx_file_resources_scope ON public.file_resources (scope, scope_id);
