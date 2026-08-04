-- Enforce at most one active (non-soft-deleted) user per email address.
-- Partial index excludes NULL emails (email is optional) and soft-deleted rows so a released email
-- can be reused, while blocking concurrent duplicate inserts/updates that the application-level
-- email pre-check cannot prevent under READ COMMITTED (M-13). Matches the exact-match semantics of
-- UserRepository.findByEmail (no lower() normalization).
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active
    ON public.users (email)
    WHERE email IS NOT NULL AND deleted_time IS NULL;
