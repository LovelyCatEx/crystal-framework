package com.lovelycatv.crystalframework.monitor.types

enum class MetricType(val tableName: String) {
    CPU_USAGE("monitor_cpu_usage"),
    CPU_LOAD_AVERAGE("monitor_cpu_load_average"),
    MEMORY_USED("monitor_memory_used"),
    JVM_HEAP_USED("monitor_jvm_heap_used"),
    JVM_NONHEAP_COMMITTED("monitor_jvm_nonheap_committed"),
    JVM_NONHEAP_USED("monitor_jvm_nonheap_used"),
    DISK_USED("monitor_disk_used"),
    DB_CONNECTIONS_ACTIVE("monitor_db_connections_active"),
    GC_COUNT("monitor_gc_count"),
    GC_TIME("monitor_gc_time"),
}
