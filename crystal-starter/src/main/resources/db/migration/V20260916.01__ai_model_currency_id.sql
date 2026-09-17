-- Replace the loose string currency code on AI model / invocation-record tables with a numeric
-- currency_id referencing economy_currencies, backfilling existing codes where a matching,
-- non-deleted currency row exists. currency_id = 0 is the sentinel for "no currency" (see
-- CurrencyConstants.NO_CURRENCY_ID); snowflake ids are always positive.

-- ai_models
ALTER TABLE public.ai_models ADD COLUMN currency_id bigint;
UPDATE public.ai_models m
   SET currency_id = c.id
  FROM public.economy_currencies c
 WHERE c.code = m.currency AND c.deleted_time IS NULL;
UPDATE public.ai_models SET currency_id = 0 WHERE currency_id IS NULL;
ALTER TABLE public.ai_models ALTER COLUMN currency_id SET NOT NULL;
ALTER TABLE public.ai_models ALTER COLUMN currency_id SET DEFAULT 0;
ALTER TABLE public.ai_models DROP COLUMN currency;

-- ai_model_invocation_records
ALTER TABLE public.ai_model_invocation_records ADD COLUMN currency_id bigint;
UPDATE public.ai_model_invocation_records r
   SET currency_id = c.id
  FROM public.economy_currencies c
 WHERE c.code = r.currency AND c.deleted_time IS NULL;
UPDATE public.ai_model_invocation_records SET currency_id = 0 WHERE currency_id IS NULL;
ALTER TABLE public.ai_model_invocation_records ALTER COLUMN currency_id SET NOT NULL;
ALTER TABLE public.ai_model_invocation_records ALTER COLUMN currency_id SET DEFAULT 0;
ALTER TABLE public.ai_model_invocation_records DROP COLUMN currency;
