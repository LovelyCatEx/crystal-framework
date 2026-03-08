import React, {type JSX} from "react";
import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {FileResource} from "@/types/file-resource.types.ts";
import {ResourceFileType} from "@/types/file-resource.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/swr.ts";
import {StorageProviderManagerController} from "@/api/storage-provider.api.ts";
import {UserManagerController} from "@/api/user.api.ts";
import {type StorageProvider} from "@/types/storage-provider.types.ts";
import type {User} from "@/types/user.types.ts";
import {StorageProviderCard, UserCard} from "../card/pop";
import {UserAvatar} from "../UserAvatar.tsx";

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
        return (
            <Popover content={<StorageProviderCard providerId={providerId} />} placement="right" trigger="hover">
                <Tag color="blue" className="m-0 leading-4 h-4 px-1 rounded cursor-pointer">
                    {provider.name}
                </Tag>
            </Popover>
        );
    }

    return <CopyableToolTip title={providerId}>
        <Tag color="blue" className="m-0 leading-4 h-4 px-1 rounded">
            提供商ID: {providerId}
        </Tag>
    </CopyableToolTip>;
}

function UserCell({ userId }: { userId: string }) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        `file-resource-user-${userId}`,
        async () => {
            return await UserManagerController.getById(userId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userId} />} placement="right" trigger="hover">
                <Space orientation="horizontal" size={8} className="cursor-pointer">
                    <UserAvatar fileEntityId={user.avatar} />
                    <Space orientation="vertical" size={0}>
                        <span className="text-xs font-mono font-bold">{user.nickname}</span>
                        <span className="text-xs text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    return <CopyableToolTip title={userId}>
        <Tag color="green" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
            用户ID: {userId}
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
                    <span className="text-xs font-mono">{row.fileName}.{row.fileExtension}</span>
                </CopyableToolTip>
                <span className="text-xs font-mono">大小: {formatFileSize(Number.parseInt(row.fileSize))}</span>
                <CopyableToolTip title={row.md5}>
                    <span className="text-xs font-mono">MD5: {row.md5}</span>
                </CopyableToolTip>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "上传者",
        dataIndex: "userId",
        key: "userId",
        render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
            return <UserCell userId={row.userId} />;
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
