import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {Col, Row, Segmented, theme} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {BusinessStatistics} from "@/components/dashboard/BusinessStatistics.tsx";
import {MyJoinedTenants} from "@/components/dashboard/MyJoinedTenants.tsx";
import {SystemAnnouncements} from "@/components/dashboard/SystemAnnouncements.tsx";
import {ClockCircleOutlined} from "@ant-design/icons";

const { useToken } = theme;

function getGreeting(t: ReturnType<typeof useTranslation>['t']): string {
    const hour = new Date().getHours();
    if (hour < 6) return t('components.dashboard.greeting.earlyMorning');
    if (hour < 9) return t('components.dashboard.greeting.morning');
    if (hour < 12) return t('components.dashboard.greeting.lateMorning');
    if (hour < 18) return t('components.dashboard.greeting.afternoon');
    return t('components.dashboard.greeting.evening');
}

const COMPONENT_DASHBOARD_BUSINESS_STATISTICS = "dashboard.business.statistics";
const COMPONENT_DASHBOARD_MY_TENANTS = "dashboard.tenant.joined";
const COMPONENT_DASHBOARD_ANNOUNCEMENTS = "dashboard.announcements";

function LiveClock() {
    const { token } = useToken();
    const [time, setTime] = useState(new Date());
    const timerRef = useRef<number | null>(null);

    useEffect(() => {
        timerRef.current = window.setInterval(() => {
            setTime(new Date());
        }, 1000);

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, []);

    const formattedTime = time.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false
    });

    return (
        <div className="flex items-center gap-2">
            <ClockCircleOutlined style={{ color: token.colorTextSecondary, fontSize: 20 }} />
            <span
                className="text-lg font-medium mr-2"
                style={{ color: token.colorText }}
            >
                {formattedTime}
            </span>
        </div>
    );
}

export default function DashboardPage() {
    const { t } = useTranslation();
    const [timeRange, setTimeRange] = useState("1m");
    const { accessibleComponentPaths, userProfile } = useLoggedUser();

    const timeRangeOptions = useMemo(() => [
        { label: t('components.dashboard.timeRange.1d'), value: "1d" },
        { label: t('components.dashboard.timeRange.3d'), value: "3d" },
        { label: t('components.dashboard.timeRange.5d'), value: "5d" },
        { label: t('components.dashboard.timeRange.1w'), value: "1w" },
        { label: t('components.dashboard.timeRange.2w'), value: "2w" },
        { label: t('components.dashboard.timeRange.1m'), value: "1m" },
        { label: t('components.dashboard.timeRange.3m'), value: "3m" },
        { label: t('components.dashboard.timeRange.6m'), value: "6m" },
        { label: t('components.dashboard.timeRange.1y'), value: "1y" },
    ], [t]);

    const greetingTitle = useMemo(() => {
        const nickname = userProfile?.nickname || userProfile?.username || t('components.dashboard.greeting.user');
        return `${getGreeting(t)}, ${nickname}~`;
    }, [userProfile, t]);

    const hasBusinessStatsPermission = useMemo(() => {
        return accessibleComponentPaths?.includes(COMPONENT_DASHBOARD_BUSINESS_STATISTICS);
    }, [accessibleComponentPaths]);

    const hasMyTenantsPermission = useMemo(() => {
        return accessibleComponentPaths?.includes(COMPONENT_DASHBOARD_MY_TENANTS);
    }, [accessibleComponentPaths]);

    const hasAnnouncementsPermission = useMemo(() => {
        return accessibleComponentPaths?.includes(COMPONENT_DASHBOARD_ANNOUNCEMENTS);
    }, [accessibleComponentPaths]);

    return (
        <>
            <ActionBarComponent title={greetingTitle} titleActions={<LiveClock />} />

            <div className="animate-in slide-in-from-bottom-4 duration-500">
                {hasBusinessStatsPermission && (
                    <div className="mb-6 flex">
                        <Segmented
                            options={timeRangeOptions}
                            value={timeRange}
                            onChange={(value) => setTimeRange(value as string)}
                        />
                    </div>
                )}

                {hasBusinessStatsPermission && <BusinessStatistics timeRange={timeRange} />}

                <Row gutter={[16, 16]} align="top">
                    {hasMyTenantsPermission && (
                        <Col xs={24} lg={16}>
                            <MyJoinedTenants />
                        </Col>
                    )}
                    {hasAnnouncementsPermission && (
                        <Col xs={24} lg={8}>
                            <SystemAnnouncements />
                        </Col>
                    )}
                </Row>
            </div>
        </>
    );
}
