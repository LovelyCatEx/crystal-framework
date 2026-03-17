import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {UserPermission} from "@/types/user-permission.types.ts";
import {getPermissionType} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useUserPermissionTableColumns(): EntityTableColumns<UserPermission> {
    const { t } = useTranslation();
    
    return [
        {
            title: t('components.columns.userPermission.permission'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.name}>
                        <span className="text-xs font-mono">{row.name}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.userPermission.type'),
            dataIndex: "type",
            key: "type",
            render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                const typeColors: Record<number, string> = {
                    0: 'blue',
                    1: 'green',
                    2: 'purple'
                };
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={getPermissionType(row.type)}>
                        <Tag color={typeColors[row.type] || 'default'} className="text-xs font-mono">{getPermissionType(row.type)}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.userPermission.description'),
            dataIndex: "description",
            key: "description",
            render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.description}>
                        <span className="text-xs font-mono">{row.description}</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.userPermission.path'),
            dataIndex: "path",
            key: "path",
            render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.path}>
                        <span className="text-xs font-mono">{row.path}</span>
                    </CopyableToolTip>
                </Space>
            }
        }
    ];
}
