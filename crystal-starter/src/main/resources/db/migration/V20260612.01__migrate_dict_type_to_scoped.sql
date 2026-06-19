-- Migrate tenant_dict_type from tenant_id to scope + scope_id pattern
ALTER TABLE tenant_dict_type ADD COLUMN scope INT NOT NULL DEFAULT 1;
UPDATE tenant_dict_type SET scope = 0 WHERE tenant_id = 0;
ALTER TABLE tenant_dict_type RENAME COLUMN tenant_id TO scope_id;

DROP INDEX IF EXISTS uk_tenant_dict_type_code;
DROP INDEX IF EXISTS idx_tenant_dict_type_tenant;
CREATE UNIQUE INDEX uk_dict_type_scope_code ON tenant_dict_type (scope, scope_id, code) WHERE deleted_time IS NULL;
CREATE INDEX idx_dict_type_scope ON tenant_dict_type (scope, scope_id);
