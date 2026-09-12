-- Create AI model invocation records table
CREATE TABLE ai_model_invocation_records (
    id BIGINT PRIMARY KEY,

    -- 基础信息
    request_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT NULL,
    session_id VARCHAR(255) DEFAULT NULL,
    provider_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,

    -- Token 统计
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    cached_prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    reasoning_tokens INTEGER NOT NULL DEFAULT 0,
    cache_creation_tokens INTEGER NOT NULL DEFAULT 0,

    -- 调用信息
    tool_calls_count INTEGER NOT NULL DEFAULT 0,
    message_count INTEGER NOT NULL DEFAULT 0,
    is_streaming BOOLEAN NOT NULL DEFAULT FALSE,

    -- 性能指标
    time_to_first_token_ms BIGINT NOT NULL DEFAULT 0,
    total_duration_ms BIGINT NOT NULL DEFAULT 0,
    queue_wait_ms BIGINT DEFAULT NULL,
    tokens_per_second DOUBLE PRECISION DEFAULT NULL,

    -- 成本计算
    prompt_unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    completion_unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    cache_read_unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    cache_write_unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    group_id BIGINT DEFAULT NULL,
    group_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    raw_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    final_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',

    -- 请求参数
    temperature DOUBLE PRECISION DEFAULT NULL,
    top_p DOUBLE PRECISION DEFAULT NULL,
    max_tokens INTEGER DEFAULT NULL,

    -- 执行状态
    status VARCHAR(50) NOT NULL DEFAULT 'success',
    error_code VARCHAR(100) DEFAULT NULL,
    error_message TEXT DEFAULT NULL,
    stop_reason VARCHAR(100) DEFAULT NULL,

    -- 客户端信息
    client_ip VARCHAR(255) DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,

    -- 请求响应大小
    request_size_bytes BIGINT DEFAULT NULL,
    response_size_bytes BIGINT DEFAULT NULL,

    -- BaseEntity 字段
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);

-- 索引
CREATE INDEX idx_ai_invocation_user_id ON ai_model_invocation_records(user_id);
CREATE INDEX idx_ai_invocation_tenant_id ON ai_model_invocation_records(tenant_id);
CREATE INDEX idx_ai_invocation_model_id ON ai_model_invocation_records(model_id);
CREATE INDEX idx_ai_invocation_provider_id ON ai_model_invocation_records(provider_id);
CREATE INDEX idx_ai_invocation_session_id ON ai_model_invocation_records(session_id);
CREATE INDEX idx_ai_invocation_status ON ai_model_invocation_records(status);
CREATE INDEX idx_ai_invocation_created_time ON ai_model_invocation_records(created_time);
CREATE INDEX idx_ai_invocation_group_id ON ai_model_invocation_records(group_id);
