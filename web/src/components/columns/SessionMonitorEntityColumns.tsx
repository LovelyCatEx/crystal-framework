import {CopyableToolTip} from "../CopyableToolTip.tsx";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {SessionDescription} from "@/types/system/session.types.ts";
import {SessionType} from "@/types/system/session.types.ts";
import {getSessionType} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";
import {Popover, Space, Spin, Tag} from "antd";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../resource/AvatarResource.tsx";
import {TenantManagerController} from "@/api/tenant/tenant.api.ts";
import type {Tenant} from "@/types/tenant/tenant.types.ts";
import {DesktopOutlined, GlobalOutlined} from "@ant-design/icons";

function parseUserAgent(userAgent: string): { os: string; browser: string; device: string } | null {
    if (!userAgent) return null;

    const ua = userAgent;

    let os = "Unknown";
    if (ua.includes("Windows NT 10.0")) os = "Windows 10";
    else if (ua.includes("Windows NT 6.1")) os = "Windows 7";
    else if (ua.includes("Windows NT 6.2")) os = "Windows 8";
    else if (ua.includes("Windows NT 6.3")) os = "Windows 8.1";
    else if (ua.includes("Windows NT")) os = "Windows";
    else if (ua.includes("Mac OS X")) os = "macOS";
    else if (ua.includes("iPhone")) os = "iOS";
    else if (ua.includes("iPad")) os = "iPadOS";
    else if (ua.includes("Android")) os = "Android";
    else if (ua.includes("Linux")) os = "Linux";

    let browser = "Unknown";
    if (ua.includes("Edg/")) browser = "Edge";
    else if (ua.includes("Firefox/")) browser = "Firefox";
    else if (ua.includes("Safari/") && !ua.includes("Chrome/") && !ua.includes("Edg/")) browser = "Safari";
    else if (ua.includes("Chrome/")) browser = "Chrome";
    else if (ua.includes("MSIE") || ua.includes("Trident/")) browser = "IE";

    let device = "PC";
    if (ua.includes("iPhone") || ua.includes("Android") && ua.includes("Mobile")) device = "Mobile";
    else if (ua.includes("iPad")) device = "Tablet";

    return { os, browser, device };
}

function SessionUserAgentCell({ userAgent }: { userAgent: string }) {
    const parsed = parseUserAgent(userAgent);

    const getOsColor = (os: string) => {
        switch (os) {
            case "Windows 10":
            case "Windows 7":
            case "Windows 8":
            case "Windows 8.1":
            case "Windows":
                return "blue";
            case "macOS":
                return "purple";
            case "iOS":
                return "green";
            case "Android":
                return "orange";
            case "Linux":
                return "cyan";
            default:
                return "default";
        }
    };

    const getBrowserColor = (browser: string) => {
        switch (browser) {
            case "Chrome":
                return "success";
            case "Edge":
                return "processing";
            case "Firefox":
                return "orange";
            case "Safari":
                return "cyan";
            case "IE":
                return "red";
            default:
                return "default";
        }
    };

    if (!userAgent) {
        return <span className="text-gray-400">-</span>;
    }

    return (
        <CopyableToolTip title={userAgent}>
            <Space size={4} wrap>
                {parsed && (
                    <>
                        {parsed.device === "Mobile" && (
                            <Tag color="gold">📱 Mobile</Tag>
                        )}
                        {parsed.device === "Tablet" && (
                            <Tag color="gold">📟 Tablet</Tag>
                        )}
                        <Tag color={getOsColor(parsed.os)}>
                            <DesktopOutlined /> {parsed.os}
                        </Tag>
                        <Tag color={getBrowserColor(parsed.browser)}>
                            <GlobalOutlined /> {parsed.browser}
                        </Tag>
                    </>
                )}
                {!parsed && (
                    <Tag color="default">Unknown</Tag>
                )}
            </Space>
        </CopyableToolTip>
    );
}

function SessionUserCell({ userId }: { userId: string | null }) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        userId ? `session-user-${userId}` : undefined,
        async () => {
            return await UserManagerController.getById(userId!);
        }
    );

    if (!userId) {
        return <span className="text-gray-400">-</span>;
    }

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userId} />} placement="right" trigger="hover">
                <Space size={8} className="cursor-pointer">
                    <AvatarResource fileEntityId={user.avatar} />
                    <Space orientation="vertical" size={0}>
                        <span className="text-xs font-mono font-bold">{user.nickname}</span>
                        <span className="text-xs text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    return <Space orientation='vertical' size={0}>
        <CopyableToolTip title={userId}>
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {userId}</Tag>
        </CopyableToolTip>
    </Space>;
}

function SessionTenantCell({ tenantId }: { tenantId: string | null }) {
    const shouldFetch = tenantId != null && Number(tenantId) > 0;

    // Hook must run on every render — feeding `undefined` as key tells SWR to skip fetching.
    const { data: tenant, isLoading } = useSWRComposition<Tenant | null>(
        shouldFetch ? `session-tenant-${tenantId}` : undefined,
        async () => {
            return await TenantManagerController.getById(tenantId!);
        }
    );

    if (!shouldFetch) {
        return <span className="text-gray-400">-</span>;
    }

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (tenant) {
        return <CopyableToolTip title={tenant.name}>
            <Tag color="green" className="text-xs font-mono">{tenant.name}</Tag>
        </CopyableToolTip>;
    }

    return <CopyableToolTip title={tenantId}>
        <Tag color="purple" className="text-xs font-mono">{tenantId}</Tag>
    </CopyableToolTip>;
}

export function useSessionMonitorTableColumns(): EntityTableColumns<SessionDescription> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.sessionMonitor.sessionId'),
            dataIndex: "sessionId",
            key: "sessionId",
            width: 120,
            render: (_: unknown, record: SessionDescription) => (
                <CopyableToolTip title={record.sessionId}>
                    <Tag color="purple" className="text-xs font-mono">{record.sessionId}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.sessionMonitor.type'),
            dataIndex: "type",
            key: "type",
            width: 120,
            render: (_: unknown, record: SessionDescription) => {
                const color = record.type === SessionType.USER ? 'blue' : 'orange';
                return <Tag color={color}>{getSessionType(record.type)}</Tag>;
            }
        },
        {
            title: t('components.columns.sessionMonitor.user'),
            dataIndex: "userId",
            key: "userId",
            width: 200,
            render: (_: unknown, record: SessionDescription) => (
                <SessionUserCell userId={record.userId} />
            )
        },
        {
            title: t('components.columns.sessionMonitor.tenant'),
            dataIndex: "tenantId",
            key: "tenantId",
            width: 200,
            render: (_: unknown, record: SessionDescription) => (
                <SessionTenantCell tenantId={record.tenantId} />
            )
        },
        {
            title: t('components.columns.sessionMonitor.remoteIp'),
            dataIndex: "remoteIp",
            key: "remoteIp",
            width: 180,
            render: (_: unknown, record: SessionDescription) => (
                <CopyableToolTip title={record.remoteIp}>
                    <Tag color="blue">{record.remoteIp}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.sessionMonitor.userAgent'),
            dataIndex: "userAgent",
            key: "userAgent",

            render: (_: unknown, record: SessionDescription) => (
                <Space orientation="vertical" size={2}>
                    <SessionUserAgentCell userAgent={record.userAgent} />
                    <span>{record.userAgent}</span>
                </Space>
            )
        }
    ];
}