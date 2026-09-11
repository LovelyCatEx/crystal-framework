-- Change status from varchar to integer
-- 0: failed, 1: success

-- Step 1: Drop default constraint
ALTER TABLE ai_model_invocation_records
    ALTER COLUMN status DROP DEFAULT;

-- Step 2: Convert type
ALTER TABLE ai_model_invocation_records
    ALTER COLUMN status TYPE INTEGER USING (
        CASE
            WHEN status = 'success' THEN 1
            WHEN status = 'failed' THEN 0
            ELSE 0
        END
    );

-- Step 3: Set new default value
ALTER TABLE ai_model_invocation_records
    ALTER COLUMN status SET DEFAULT 1;
