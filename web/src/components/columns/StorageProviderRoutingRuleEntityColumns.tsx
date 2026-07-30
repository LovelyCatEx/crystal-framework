import React, {useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import {useTranslation} from "react-i18next";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {StorageProviderRoutingRule} from "@/types/resource/storage-provider-routing-rule.types.ts";
import type {StorageProvider} from "@/types/resource/storage-provider.types.ts";
import {StorageProviderManagerController} from "@/api/resource/storage-provider.api.ts";
import {getRuleDistributionType} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

const TargetProvidersDisplay: React.FC<{ targetProviderIds: string }> = ({targetProviderIds}) => {
    const {t} = useTranslation();
    const [providers, setProviders] = useState<StorageProvider[]>([]);
    const [ids, setIds] = useState<string[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let parsedIds: string[] = [];
        try {
            parsedIds = (JSON.parse(targetProviderIds) as unknown[]).map((id) => String(id));
        } catch {
            parsedIds = [];
        }
        setIds(parsedIds);

        if (parsedIds.length === 0) {
            setProviders([]);
            setLoading(false);
            return;
        }

        setLoading(true);
        Promise.all(parsedIds.map((id) => StorageProviderManagerController.getById(id)))
            .then((results) => setProviders(results.filter((it): it is StorageProvider => it !== null)))
            .finally(() => setLoading(false));
    }, [targetProviderIds]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (providers.length === 0 && ids.length > 0) {
        return (
            <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                {t('components.columns.storageProviderRoutingRule.unknownProvider')}
            </Tag>
        );
    }

    return (
        <Space wrap size={4}>
            {providers.map((provider) => (
                <CopyableToolTip key={provider.id} title={provider.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{provider.name}</Tag>
                </CopyableToolTip>
            ))}
        </Space>
    );
};

export function useStorageProviderRoutingRuleTableColumns(): EntityTableColumns<StorageProviderRoutingRule> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.storageProviderRoutingRule.priority'),
            dataIndex: "priority",
            key: "priority",
            width: 90,
            render: function (_: unknown, row: StorageProviderRoutingRule): React.ReactNode {
                return <Tag color="default" className="text-xs font-mono">{row.priority}</Tag>;
            }
        },
        {
            title: t('components.columns.storageProviderRoutingRule.name'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: StorageProviderRoutingRule): React.ReactNode {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.name}>
                        <span className="text-xs font-mono font-bold">{row.name}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>;
            }
        },
        {
            title: t('components.columns.storageProviderRoutingRule.targetProviders'),
            dataIndex: "targetProviderIds",
            key: "targetProviderIds",
            render: function (_: unknown, row: StorageProviderRoutingRule): React.ReactNode {
                return <TargetProvidersDisplay targetProviderIds={row.targetProviderIds} />;
            }
        },
        {
            title: t('components.columns.storageProviderRoutingRule.distributionType'),
            dataIndex: "distributionType",
            key: "distributionType",
            render: function (_: unknown, row: StorageProviderRoutingRule): React.ReactNode {
                return <Tag color="purple" className="text-xs font-mono">{getRuleDistributionType(row.distributionType)}</Tag>;
            }
        },
    ];
}

export {TargetProvidersDisplay};
