// Actuator metrics order - same order as in the original map
export const actuatorMetricsOrder = [
    // System
    'application.started.time',
    'application.ready.time',
    'process.start.time',
    'process.uptime',

    // CPU
    'system.cpu.usage',
    'system.cpu.count',
    'system.load.average.1m',
    'process.cpu.usage',
    'process.cpu.time',

    // Disk
    'disk.total',
    'disk.free',

    // JVM Memory
    'jvm.info',
    'jvm.memory.used',
    'jvm.memory.max',
    'jvm.memory.committed',
    'jvm.memory.usage.after.gc',

    // JVM Heap
    'jvm.buffer.count',
    'jvm.buffer.memory.used',
    'jvm.buffer.total.capacity',

    // GC
    'jvm.gc.pause',
    'jvm.gc.overhead',
    'jvm.gc.memory.allocated',
    'jvm.gc.memory.promoted',
    'jvm.gc.live.data.size',
    'jvm.gc.max.data.size',
    'jvm.gc.concurrent.phase.time',

    // Class Loader
    'jvm.classes.loaded',
    'jvm.classes.loaded.count',
    'jvm.classes.unloaded',

    // Complier
    'jvm.compilation.time',

    // Thread
    'jvm.threads.live',
    'jvm.threads.peak',
    'jvm.threads.daemon',
    'jvm.threads.started',
    'jvm.threads.states',

    // Executor
    'executor.active',
    'executor.completed',
    'executor.pool.core',
    'executor.pool.max',
    'executor.pool.size',
    'executor.queue.remaining',
    'executor.queued',

    // HTTP Request
    'http.server.requests',
    'http.server.requests.active',

    // R2DBC Connection Pool
    'r2dbc.pool.acquired',
    'r2dbc.pool.allocated',
    'r2dbc.pool.idle',
    'r2dbc.pool.max.allocated',
    'r2dbc.pool.pending',
    'r2dbc.pool.max.pending',

    // Redis (Lettuce)
    'lettuce',
    'lettuce.active',

    // Spring Data
    'spring.data.repository.invocations',

    // Spring Integration
    'spring.integration.channels',
    'spring.integration.handlers',
    'spring.integration.sources',

    // Spring Security
    'spring.security.authorizations',
    'spring.security.authorizations.active',
    'spring.security.filterchains',
    'spring.security.filterchains.active',
    'spring.security.http.secured.requests',
    'spring.security.http.secured.requests.active',

    // Logs
    'logback.events',

    // Files
    'process.files.max',
    'process.files.open'
];
