import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {StorageProvider} from "../../types/storage-provider.types.ts";
import {StorageProviderType} from "../../types/storage-provider.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

export const STORAGE_PROVIDER_MANAGER_TABLE_COLUMNS: EntityTableColumns<StorageProvider> = [
    {
        title: "名称",
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
        title: "类型",
        dataIndex: "type",
        key: "type",
        render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={StorageProviderType[row.type]}>
                    <Tag color="orange" className="text-xs font-mono">{StorageProviderType[row.type]}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.description ?? '无描述'}>
                    <span className="text-xs font-mono">{row.description ?? '-'}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "基础URL",
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
        title: "配置",
        dataIndex: "properties",
        key: "properties",
        render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.properties}>
                <span className="text-xs font-mono text-gray-500">{row.properties.substring(0, 32)}...</span>
            </CopyableToolTip>
        }
    }
];
