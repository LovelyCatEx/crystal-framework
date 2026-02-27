export const actuatorMetricsToTranslationMap = new Map<string, string>([
    // System
    ['application.started.time', '应用启动时间'],
    ['application.ready.time', '应用就绪时间'],
    ['process.start.time', '进程启动时间'],
    ['process.uptime', '进程运行时长'],

    // CPU
    ['system.cpu.usage', '系统 CPU 使用率'],
    ['system.cpu.count', 'CPU 核心数'],
    ['system.load.average.1m', '1分钟平均负载'],
    ['process.cpu.usage', '进程 CPU 使用率'],
    ['process.cpu.time', '进程 CPU 总时间'],

    // Disk
    ['disk.total', '磁盘总容量'],
    ['disk.free', '磁盘可用容量'],

    // JVM Memory
    ['jvm.info', 'JVM 版本信息'],
    ['jvm.memory.used', 'JVM 已用内存'],
    ['jvm.memory.max', 'JVM 最大内存'],
    ['jvm.memory.committed', 'JVM 已分配内存'],
    ['jvm.memory.usage.after.gc', 'GC 后内存使用'],

    // JVM Heap
    ['jvm.buffer.count', '缓冲区数量'],
    ['jvm.buffer.memory.used', '缓冲区已用内存'],
    ['jvm.buffer.total.capacity', '缓冲区总容量'],

    // GC
    ['jvm.gc.pause', 'GC 暂停时间'],
    ['jvm.gc.overhead', 'GC 开销'],
    ['jvm.gc.memory.allocated', 'GC 分配内存'],
    ['jvm.gc.memory.promoted', 'GC 晋升内存'],
    ['jvm.gc.live.data.size', 'GC 存活数据大小'],
    ['jvm.gc.max.data.size', 'GC 最大数据大小'],
    ['jvm.gc.concurrent.phase.time', 'GC 并发阶段时间'],

    // Class Loader
    ['jvm.classes.loaded', '已加载类数量'],
    ['jvm.classes.loaded.count', '已加载类总数'],
    ['jvm.classes.unloaded', '已卸载类数量'],

    // Complier
    ['jvm.compilation.time', 'JIT 编译时间'],

    // Thread
    ['jvm.threads.live', '活跃线程数'],
    ['jvm.threads.peak', '峰值线程数'],
    ['jvm.threads.daemon', '守护线程数'],
    ['jvm.threads.started', '已启动线程总数'],
    ['jvm.threads.states', '各状态线程数'],

    // Executor
    ['executor.active', '活跃线程数'],
    ['executor.completed', '已完成任务数'],
    ['executor.pool.core', '核心线程数'],
    ['executor.pool.max', '最大线程数'],
    ['executor.pool.size', '当前线程池大小'],
    ['executor.queue.remaining', '队列剩余容量'],
    ['executor.queued', '队列中任务数'],

    // HTTP Request
    ['http.server.requests', 'HTTP 请求统计'],
    ['http.server.requests.active', '活跃请求数'],

    // R2DBC Connection Pool
    ['r2dbc.pool.acquired', '已获取连接数'],
    ['r2dbc.pool.allocated', '已分配连接数'],
    ['r2dbc.pool.idle', '空闲连接数'],
    ['r2dbc.pool.max.allocated', '最大分配连接数'],
    ['r2dbc.pool.pending', '等待连接数'],
    ['r2dbc.pool.max.pending', '最大等待连接数'],

    // Redis (Lettuce)
    ['lettuce', 'Redis 连接数'],
    ['lettuce.active', 'Redis 活跃连接'],

    // Spring Data
    ['spring.data.repository.invocations', 'Repository 调用次数'],

    // Spring Integration
    ['spring.integration.channels', '集成通道数'],
    ['spring.integration.handlers', '集成处理器数'],
    ['spring.integration.sources', '集成源数量'],

    // Spring Security
    ['spring.security.authorizations', '授权次数'],
    ['spring.security.authorizations.active', '活跃授权数'],
    ['spring.security.filterchains', '过滤器链'],
    ['spring.security.filterchains.active', '活跃过滤器链'],
    ['spring.security.http.secured.requests', '安全请求统计'],
    ['spring.security.http.secured.requests.active', '活跃安全请求'],

    // Logs
    ['logback.events', '日志事件数'],

    // Files
    ['process.files.max', '最大文件句柄数'],
    ['process.files.open', '已打开文件句柄数']
]);