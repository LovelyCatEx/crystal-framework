import React, {type JSX} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {Tenant, TenantTireType} from "@/types/tenant.types.ts";
import {getTenantStatus} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/swr.ts";
import {TenantTireTypeManagerController} from "@/api/tenant-tire-type.api.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {ShopOutlined} from "@ant-design/icons";
import {AvatarResource} from "@/components/AvatarResource.tsx";
import {useTranslation} from "react-i18next";

function TireTypeCell({ tireTypeId }: { tireTypeId: string }) {
    const { data: tireType, isLoading } = useSWRComposition<TenantTireType | null>(
        `tenant-tire-type-${tireTypeId}`,
        async () => {
            return await TenantTireTypeManagerController.getById(tireTypeId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (tireType) {
        return <CopyableToolTip title={tireType.name}>
            <Tag className="font-mono font-bold" color="blue">{tireType.name}</Tag>
        </CopyableToolTip>;
    }

    return <CopyableToolTip title={tireTypeId}>
        <Tag color="purple" className="font-mono">{tireTypeId}</Tag>
    </CopyableToolTip>;
}

export function useTenantTableColumns(): EntityTableColumns<Tenant> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenant.tenantName'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                return <Space orientation="horizontal" size={8}>
                    <AvatarResource
                        fileEntityId={row?.icon}
                        defaultIcon={<ShopOutlined />}
                    />
                    <Space orientation='vertical' size={0}>
                        <CopyableToolTip title={row.name}>
                            <span className="text-xs font-mono font-bold">{row.name}</span>
                        </CopyableToolTip>
                        <CopyableToolTip title={row.id}>
                            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                        </CopyableToolTip>
                    </Space>
                </Space>
            }
        },
        {
            title: t('components.columns.tenant.description'),
            dataIndex: "description",
            key: "description",
            width: 200,
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.description ?? '-'}>
                        <span className="text-xs font-mono">{
                            ((row.description?.length || 0) > 64
                                ? (row.description?.slice(0, 64) + '...')
                                : row.description
                            )
                            ?? '-'
                        }</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.tenant.status'),
            dataIndex: "status",
            key: "status",
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                const statusColors: Record<number, string> = {
                    0: 'orange',
                    1: 'green',
                    2: 'red'
                };
                return <Tag color={statusColors[row.status] || 'default'} className="text-xs font-mono">
                    {getTenantStatus(row.status)}
                </Tag>
            }
        },
        {
            title: t('components.columns.tenant.tireType'),
            dataIndex: "tireTypeId",
            key: "tireTypeId",
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                return <TireTypeCell tireTypeId={row.tireTypeId} />;
            }
        },
        {
            title: t('components.columns.tenant.contact'),
            dataIndex: "contactName",
            key: "contactName",
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.contactName}>
                        <span className="text-xs font-mono font-bold">{row.contactName}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.contactEmail}>
                        <span className="text-xs font-mono text-gray-500">{row.contactEmail}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.contactPhone}>
                        <span className="text-xs font-mono text-gray-500">{row.contactPhone}</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.tenant.address'),
            dataIndex: "address",
            key: "address",
            width: 200,
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.address}>
                    <span className="text-xs font-mono">{row.address}</span>
                </CopyableToolTip>
            }
        },
        {
            title: t('components.columns.tenant.subscriptionInfo'),
            dataIndex: "subscribedTime",
            key: "subscribedTime",
            width: 240,
            render: function (_: unknown, row: Tenant): React.ReactNode | JSX.Element {
                const expiresTime = Number(row.expiresTime);
                const now = Date.now();
                const isExpired = expiresTime < now;

                return <Space orientation='vertical' size={0}>
                    <span className="text-xs font-mono">
                        {t('components.columns.tenant.subscribedTime')}: {formatTimestamp(row.subscribedTime, 'YYYY-MM-DD')}
                    </span>
                    <span className={`text-xs font-mono ${isExpired ? 'text-red-500' : ''}`}>
                        {t('components.columns.tenant.expiresTime')}: {formatTimestamp(row.expiresTime, 'YYYY-MM-DD')}
                        {isExpired && <Tag color="red" className="ml-2 text-xs">{t('components.columns.tenant.expired')}</Tag>}
                    </span>
                </Space>
            }
        }
    ];
}
