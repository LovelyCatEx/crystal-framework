-- Per-instance snapshot of the definition-level form schema captured at startFlow time.
-- Decouples in-flight instances from later edits to the definition's schema, so already-running
-- approvals keep the exact form UX (fields, types, options) they were initiated with.
-- See `.claude/research/approval-form-design-decisions.md` (option C).
ALTER TABLE approval_flow_instance ADD COLUMN form_schema_snapshot TEXT DEFAULT NULL;
