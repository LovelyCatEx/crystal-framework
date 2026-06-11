-- Approval Flow Definition
CREATE TABLE approval_flow_definition (
    id              BIGINT NOT NULL PRIMARY KEY,
    scope           SMALLINT NOT NULL DEFAULT 1,
    scope_id        BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(128) NOT NULL,
    description     VARCHAR(512) DEFAULT NULL,
    current_version INT NOT NULL DEFAULT 1,
    status          SMALLINT NOT NULL DEFAULT 0,
    form_schema     TEXT DEFAULT NULL,
    created_time    BIGINT NOT NULL,
    modified_time   BIGINT NOT NULL,
    deleted_time    BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_def_scope ON approval_flow_definition (scope, scope_id);

-- Approval Flow Node
CREATE TABLE approval_flow_node (
    id                  BIGINT NOT NULL PRIMARY KEY,
    definition_id       BIGINT NOT NULL,
    definition_version  INT NOT NULL,
    node_key            VARCHAR(64) NOT NULL,
    type                SMALLINT NOT NULL,
    name                VARCHAR(128) NOT NULL DEFAULT '',
    config              TEXT DEFAULT NULL,
    form_schema         TEXT DEFAULT NULL,
    position_x          INT NOT NULL DEFAULT 0,
    position_y          INT NOT NULL DEFAULT 0,
    created_time        BIGINT NOT NULL,
    modified_time       BIGINT NOT NULL,
    deleted_time        BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_node_def_ver ON approval_flow_node (definition_id, definition_version);

-- Approval Flow Edge
CREATE TABLE approval_flow_edge (
    id                  BIGINT NOT NULL PRIMARY KEY,
    definition_id       BIGINT NOT NULL,
    definition_version  INT NOT NULL,
    source_node_id      BIGINT NOT NULL,
    target_node_id      BIGINT NOT NULL,
    created_time        BIGINT NOT NULL,
    modified_time       BIGINT NOT NULL,
    deleted_time        BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_edge_def_ver ON approval_flow_edge (definition_id, definition_version);
CREATE INDEX idx_approval_edge_source ON approval_flow_edge (source_node_id);

-- Approval Flow Instance
CREATE TABLE approval_flow_instance (
    id                  BIGINT NOT NULL PRIMARY KEY,
    scope               SMALLINT NOT NULL DEFAULT 1,
    scope_id            BIGINT NOT NULL DEFAULT 0,
    definition_id       BIGINT NOT NULL,
    definition_version  INT NOT NULL,
    initiator_id        BIGINT NOT NULL,
    status              SMALLINT NOT NULL DEFAULT 0,
    form_data           TEXT DEFAULT NULL,
    current_node_id     BIGINT NOT NULL,
    created_time        BIGINT NOT NULL,
    modified_time       BIGINT NOT NULL,
    deleted_time        BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_inst_scope ON approval_flow_instance (scope, scope_id);
CREATE INDEX idx_approval_inst_initiator ON approval_flow_instance (initiator_id);
CREATE INDEX idx_approval_inst_status ON approval_flow_instance (status);

-- Approval Flow Task
CREATE TABLE approval_flow_task (
    id              BIGINT NOT NULL PRIMARY KEY,
    scope           SMALLINT NOT NULL DEFAULT 1,
    scope_id        BIGINT NOT NULL DEFAULT 0,
    instance_id     BIGINT NOT NULL,
    node_id         BIGINT NOT NULL,
    assignee_id     BIGINT NOT NULL,
    status          SMALLINT NOT NULL DEFAULT 0,
    comment         VARCHAR(1024) DEFAULT NULL,
    form_data       TEXT DEFAULT NULL,
    created_time    BIGINT NOT NULL,
    modified_time   BIGINT NOT NULL,
    deleted_time    BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_task_instance ON approval_flow_task (instance_id);
CREATE INDEX idx_approval_task_assignee ON approval_flow_task (assignee_id, status);

-- Approval Flow Record
CREATE TABLE approval_flow_record (
    id              BIGINT NOT NULL PRIMARY KEY,
    scope           SMALLINT NOT NULL DEFAULT 1,
    scope_id        BIGINT NOT NULL DEFAULT 0,
    instance_id     BIGINT NOT NULL,
    node_id         BIGINT NOT NULL,
    operator_id     BIGINT NOT NULL,
    action          SMALLINT NOT NULL,
    comment         VARCHAR(1024) DEFAULT NULL,
    created_time    BIGINT NOT NULL,
    modified_time   BIGINT NOT NULL,
    deleted_time    BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_record_instance ON approval_flow_record (instance_id);
