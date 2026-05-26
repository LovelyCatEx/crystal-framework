export interface DashboardStatsVO {
    businessStats: BusinessStatsVO;
    systemMetrics: SystemMetricsVO;
}

export interface BusinessStatsVO {
    totalUsers: StatItem;
    totalTenants: StatItem;
    totalTenantMembers: StatItem;
    totalFileResources: StatItem;
    totalMailSent: StatItem;
    totalInvitations: StatItem;
    totalInvitationRecords: StatItem;
    totalOAuthAccounts: StatItem;
}

export interface StatItem {
    value: number;
    change: number;
    changePercent: number;
}

export interface SystemMetricsVO {
    cpuUsage: MetricItem;
    memoryUsage: MetricItem;
    jvmHeapMemory: MetricItem;
    jvmNonHeapMemory: MetricItem;
    systemLoad: MetricItem;
    dbConnections: MetricItem;
    diskUsage: MetricItem;
    gcMetrics: GCMetricsItem;
    serverInfo: ServerInfo;
}

export interface MetricItem {
    used: number;
    total: number;
    usage: number;
}

export interface GCMetricsItem {
    avgTime: number;
    totalTime: number;
    count: number;
}

export interface ServerInfo {
    serverName: string;
    databaseVersion: string;
    redisVersion: string;
    projectVersion: string;
    uptime: string;
}