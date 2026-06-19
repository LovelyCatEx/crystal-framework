-- Rename current_node_id to latest_node_id in approval_flow_instance
ALTER TABLE approval_flow_instance RENAME COLUMN current_node_id TO latest_node_id;

-- Add token_id column to approval_flow_task
ALTER TABLE approval_flow_task ADD COLUMN token_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_approval_task_token ON approval_flow_task (token_id);

-- Create approval_flow_token table
CREATE TABLE approval_flow_token (
    id                      BIGINT NOT NULL PRIMARY KEY,
    instance_id             BIGINT NOT NULL,
    current_node_id         BIGINT NOT NULL,
    status                  SMALLINT NOT NULL DEFAULT 0,
    fork_node_id            BIGINT DEFAULT NULL,
    created_time            BIGINT NOT NULL,
    modified_time           BIGINT NOT NULL,
    deleted_time            BIGINT DEFAULT NULL
);
CREATE INDEX idx_approval_token_instance ON approval_flow_token (instance_id);
CREATE INDEX idx_approval_token_fork ON approval_flow_token (fork_node_id, current_node_id, status);
