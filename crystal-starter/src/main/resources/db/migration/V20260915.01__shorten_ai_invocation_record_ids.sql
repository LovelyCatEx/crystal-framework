ALTER TABLE public.ai_model_invocation_records
    ALTER COLUMN request_id TYPE varchar(128),
    ALTER COLUMN session_id TYPE varchar(128);
