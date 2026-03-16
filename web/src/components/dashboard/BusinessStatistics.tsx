import { Card } from "antd";
import { useEffect, useState } from "react";
import {
    CloudOutlined,
    FileOutlined,
    MailOutlined,
    SafetyOutlined,
    ShopOutlined,
    TagsOutlined,
    TeamOutlined,
    UserOutlined
} from "@ant-design/icons";
import { getBusinessStats } from "@/api/dashboard.api.ts";
import type { BusinessStatsVO } from "@/types/dashboard.types.ts";

const statCardConfig = [
    { key: "totalUsers" as keyof BusinessStatsVO, title: "总用户数", icon: <UserOutlined />, color: "blue" },
    { key: "totalTenants" as keyof BusinessStatsVO, title: "总租户数", icon: <ShopOutlined />, color: "emerald" },
    { key: "totalTenantMembers" as keyof BusinessStatsVO, title: "租户成员数", icon: <TeamOutlined />, color: "violet" },
    { key: "totalFileResources" as keyof BusinessStatsVO, title: "文件资源数", icon: <FileOutlined />, color: "orange" },
    { key: "totalMailSent" as keyof BusinessStatsVO, title: "邮件发送量", icon: <MailOutlined />, color: "cyan" },
    { key: "totalInvitations" as keyof BusinessStatsVO, title: "邀请码数", icon: <TagsOutlined />, color: "pink" },
    { key: "totalInvitationRecords" as keyof BusinessStatsVO, title: "邀请记录数", icon: <CloudOutlined />, color: "indigo" },
    { key: "totalOAuthAccounts" as keyof BusinessStatsVO, title: "OAuth 绑定数", icon: <SafetyOutlined />, color: "amber" },
];

function formatNumber(num: number): string {
    return num.toLocaleString("zh-CN");
}

function formatChange(change: number): string {
    const sign = change >= 0 ? "+" : "";
    return `${sign}${formatNumber(change)}`;
}

export interface BusinessStatisticsProps {
    timeRange: string;
}

export function BusinessStatistics({ timeRange }: BusinessStatisticsProps) {
    const [businessStats, setBusinessStats] = useState<BusinessStatsVO | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadBusinessStats();
    }, [timeRange]);

    const loadBusinessStats = async () => {
        setLoading(true);
        try {
            const res = await getBusinessStats(timeRange);
            setBusinessStats(res.data);
        } catch (error) {
            console.error("Failed to load business stats:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {statCardConfig.map((config) => {
                const statItem = businessStats?.[config.key];
                const value = statItem?.value ?? 0;
                const change = statItem?.change ?? 0;

                return (
                    <Card
                        key={config.key}
                        className="rounded-3xl border-none shadow-sm hover:shadow-md transition-all"
                        loading={loading}
                    >
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm font-bold text-slate-400 uppercase tracking-wider mb-2">
                                    {config.title}
                                </p>
                                <h2 className="text-3xl font-bold text-slate-800">
                                    {formatNumber(value)}
                                </h2>
                                <span
                                    className={`text-sm font-bold ${
                                        change >= 0 ? "text-emerald-500" : "text-red-500"
                                    }`}
                                >
                                    {formatChange(change)}
                                </span>
                            </div>
                            <div
                                className={`w-14 h-14 rounded-2xl flex items-center justify-center text-2xl
                                ${config.color === "blue" ? "bg-blue-50 text-blue-600" : ""}
                                ${config.color === "emerald" ? "bg-emerald-50 text-emerald-600" : ""}
                                ${config.color === "violet" ? "bg-violet-50 text-violet-600" : ""}
                                ${config.color === "orange" ? "bg-orange-50 text-orange-600" : ""}
                                ${config.color === "cyan" ? "bg-cyan-50 text-cyan-600" : ""}
                                ${config.color === "pink" ? "bg-pink-50 text-pink-600" : ""}
                                ${config.color === "indigo" ? "bg-indigo-50 text-indigo-600" : ""}
                                ${config.color === "amber" ? "bg-amber-50 text-amber-600" : ""}
                            `}
                            >
                                {config.icon}
                            </div>
                        </div>
                    </Card>
                );
            })}
        </div>
    );
}
