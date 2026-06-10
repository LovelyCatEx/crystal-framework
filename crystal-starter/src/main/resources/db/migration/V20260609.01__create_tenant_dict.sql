-- Tenant Dictionary Type
CREATE TABLE tenant_dict_type (
    id             BIGINT NOT NULL PRIMARY KEY,
    tenant_id      BIGINT NOT NULL DEFAULT 0,
    code           VARCHAR(64) NOT NULL,
    name           VARCHAR(128) NOT NULL,
    remark         VARCHAR(512) DEFAULT NULL,
    status         INT NOT NULL DEFAULT 1,
    created_time   BIGINT NOT NULL,
    modified_time  BIGINT NOT NULL,
    deleted_time   BIGINT DEFAULT NULL
);
CREATE UNIQUE INDEX uk_tenant_dict_type_code ON tenant_dict_type (tenant_id, code) WHERE deleted_time IS NULL;
CREATE INDEX idx_tenant_dict_type_tenant ON tenant_dict_type (tenant_id);

-- Tenant Dictionary Item
CREATE TABLE tenant_dict_item (
    id             BIGINT NOT NULL PRIMARY KEY,
    type_id        BIGINT NOT NULL,
    item_code      VARCHAR(64) NOT NULL,
    item_value     VARCHAR(256) NOT NULL,
    parent_id      BIGINT DEFAULT NULL,
    sort_order     INT NOT NULL DEFAULT 0,
    is_default     BOOLEAN NOT NULL DEFAULT FALSE,
    status         INT NOT NULL DEFAULT 1,
    created_time   BIGINT NOT NULL,
    modified_time  BIGINT NOT NULL,
    deleted_time   BIGINT DEFAULT NULL
);
CREATE UNIQUE INDEX uk_tenant_dict_item_code ON tenant_dict_item (type_id, item_code) WHERE deleted_time IS NULL;
CREATE INDEX idx_tenant_dict_item_type ON tenant_dict_item (type_id);
CREATE INDEX idx_tenant_dict_item_parent ON tenant_dict_item (parent_id);
