import { ActionBarComponent } from "@/components/ActionBarComponent.tsx";
import { Segmented } from "antd";
import { useMemo, useState } from "react";
import { useLoggedUser } from "@/compositions/use-logged-user.ts";
import { BusinessStatistics } from "@/components/dashboard/BusinessStatistics.tsx";
import { SystemMetrics } from "@/components/dashboard/SystemMetrics.tsx";

function getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 6) return "晚上好";
    if (hour < 9) return "早上好";
    if (hour < 12) return "上午好";
    if (hour < 18) return "下午好";
    return "晚上好";
}

const COMPONENT_DASHBOARD_BUSINESS_STATISTICS = "dashboard.business.statistics";
const COMPONENT_DASHBOARD_SYSTEM_METRICS = "dashboard.system.metrics";

const timeRangeOptions = [
    { label: "1天", value: "1d" },
    { label: "3天", value: "3d" },
    { label: "5天", value: "5d" },
    { label: "1周", value: "1w" },
    { label: "2周", value: "2w" },
    { label: "1月", value: "1m" },
    { label: "3月", value: "3m" },
    { label: "半年", value: "6m" },
    { label: "1年", value: "1y" },
];

export function DashboardPage() {
    const [timeRange, setTimeRange] = useState("1m");
    const { accessibleComponentPaths, userProfile } = useLoggedUser();

    const greetingTitle = useMemo(() => {
        const nickname = userProfile?.nickname || userProfile?.username || "用户";
        return `${getGreeting()}，${nickname}~`;
    }, [userProfile]);

    const hasBusinessStatsPermission = useMemo(() => {
        return accessibleComponentPaths?.includes(COMPONENT_DASHBOARD_BUSINESS_STATISTICS);
    }, [accessibleComponentPaths]);

    const hasSystemMetricsPermission = useMemo(() => {
        return accessibleComponentPaths?.includes(COMPONENT_DASHBOARD_SYSTEM_METRICS);
    }, [accessibleComponentPaths]);

    return (
        <>
            <ActionBarComponent title={greetingTitle} />

            <div className="animate-in slide-in-from-bottom-4 duration-500">
                {hasBusinessStatsPermission && (
                    <div className="mb-6 flex">
                        <Segmented
                            options={timeRangeOptions}
                            value={timeRange}
                            onChange={(value) => setTimeRange(value as string)}
                            className="bg-slate-100"
                        />
                    </div>
                )}

                {hasBusinessStatsPermission && <BusinessStatistics timeRange={timeRange} />}

                {hasSystemMetricsPermission && <SystemMetrics />}
            </div>
        </>
    );
}
