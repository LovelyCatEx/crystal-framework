import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {StorageProvider} from "@/types/resource/storage-provider.types.ts";
import {useStorageProviderTypes} from "@/compositions/use-storage-provider-types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

const BUILTIN_TYPE_COLORS: Record<number, string> = {
    0: 'blue',
    1: 'orange',
    2: 'green',
    3: 'purple',
};

export function useStorageProviderTableColumns(): EntityTableColumns<StorageProvider> {
    const { t } = useTranslation();
    const { getLabel } = useStorageProviderTypes();

    return [
        {
            title: t('components.columns.storageProvider.name'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.name}>
                        <span className="text-xs font-mono font-bold">{row.name}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.storageProvider.type'),
            dataIndex: "type",
            key: "type",
            render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                const label = getLabel(row.type);
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={label}>
                        <Tag color={BUILTIN_TYPE_COLORS[row.type] || 'default'} className="text-xs font-mono">{label}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.storageProvider.description'),
            dataIndex: "description",
            key: "description",
            width: 180,
            render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.description ?? '-'}>
                        <span className="text-xs font-mono">{row.description ?? '-'}</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.storageProvider.baseUrl'),
            dataIndex: "baseUrl",
            key: "baseUrl",
            render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.baseUrl}>
                        <span className="text-xs font-mono text-blue-600">{row.baseUrl}</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.storageProvider.config'),
            dataIndex: "properties",
            key: "properties",
            render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.properties}>
                    <span className="text-xs font-mono text-gray-500">{row.properties.substring(0, 32)}...</span>
                </CopyableToolTip>
            }
        }
    ];
}
