DROP TABLE IF EXISTS ext_system_monitor_metrics;

CREATE TABLE IF NOT EXISTS monitor_cpu_usage (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_cpu_usage_time ON monitor_cpu_usage (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_cpu_load_average (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_cpu_load_average_time ON monitor_cpu_load_average (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_memory_used (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_memory_used_time ON monitor_memory_used (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_jvm_heap_used (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_jvm_heap_used_time ON monitor_jvm_heap_used (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_jvm_nonheap_committed (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_jvm_nonheap_committed_time ON monitor_jvm_nonheap_committed (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_jvm_nonheap_used (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_jvm_nonheap_used_time ON monitor_jvm_nonheap_used (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_disk_used (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_disk_used_time ON monitor_disk_used (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_db_connections_active (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_db_connections_active_time ON monitor_db_connections_active (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_gc_count (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_gc_count_time ON monitor_gc_count (created_time DESC);

CREATE TABLE IF NOT EXISTS monitor_gc_time (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_monitor_gc_time_time ON monitor_gc_time (created_time DESC);
