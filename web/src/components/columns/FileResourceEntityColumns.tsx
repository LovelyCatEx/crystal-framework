import React, {type JSX} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {FileResource} from "../../types/file-resource.types.ts";
import {ResourceFileType} from "../../types/file-resource.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "../../compositions/swr.ts";
import {StorageProviderManagerController} from "../../api/storage-provider.api.ts";
import {type StorageProvider, StorageProviderType} from "../../types/storage-provider.types.ts";

function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function StorageProviderCell({ providerId }: { providerId: string }) {
    const { data: provider, isLoading }= useSWRComposition<StorageProvider | null>(
        `storage-provider-${providerId}`,
        async () => {
            return StorageProviderManagerController.getById(providerId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (provider) {
        return <CopyableToolTip
            title={
                <Space orientation="vertical">
                    <span>提供商ID: {providerId}</span>
                    <span>提供商类型: {StorageProviderType[provider.type]}</span>
                </Space>
            }
        >
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                {provider.name}
            </Tag>
        </CopyableToolTip>;
    }

    return <CopyableToolTip title={providerId}>
        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
            提供商ID: {providerId}
        </Tag>
    </CopyableToolTip>;
}

export const FILE_RESOURCE_MANAGER_TABLE_COLUMNS: EntityTableColumns<FileResource> = [
    {
        title: "文件信息",
        dataIndex: "fileName",
        key: "fileName",
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.fileName}>
                    <span className="text-xs font-mono">{row.fileName}</span>
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
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={ResourceFileType[row.type]}>
                    <Tag color="orange" className="text-xs font-mono">{ResourceFileType[row.type]}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "文件属性",
        dataIndex: "fileExtension",
        key: "fileExtension",
        width: 160,
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <span className="text-xs font-mono">扩展名: {row.fileExtension}</span>
                <span className="text-xs font-mono">大小: {formatFileSize(Number.parseInt(row.fileSize))}</span>
            </Space>
        }
    },
    {
        title: "MD5",
        dataIndex: "md5",
        key: "md5",
        width: 200,
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.md5}>
                <span className="text-xs font-mono">{row.md5}</span>
            </CopyableToolTip>
        }
    },
    {
        title: "存储信息",
        dataIndex: "storageProviderId",
        key: "storageProviderId",
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <StorageProviderCell providerId={row.storageProviderId} />
                <CopyableToolTip title={row.objectKey}>
                    <span className="text-xs font-mono text-gray-500">{row.objectKey.substring(0, 30)}...</span>
                </CopyableToolTip>
            </Space>
        }
    }
];
