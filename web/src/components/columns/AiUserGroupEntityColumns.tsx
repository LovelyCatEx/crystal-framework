import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiUserGroupEntity} from "@/types/ai/ai.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useAiUserGroupTableColumns(): EntityTableColumns<AiUserGroupEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('pages.aiUserGroupManager.modal.name.label'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: AiUserGroupEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <Space size={4}>
                        <span className="text-sm font-medium">{row.name}</span>
                        {row.isDefault && <Tag color="gold">{t('pages.aiUserGroupManager.modal.isDefault.label')}</Tag>}
                    </Space>
                    <CopyableToolTip title={row.key}>
                        <span className="text-xs text-gray-500 font-mono">{row.key}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('pages.aiUserGroupManager.modal.description.label'),
            dataIndex: "description",
            key: "description",
            render: function (_: unknown, row: AiUserGroupEntity): React.ReactNode | JSX.Element {
                return <span className="text-xs">{row.description || '-'}</span>
            }
        },
        {
            title: t('pages.aiUserGroupManager.modal.billingMultiplier.label'),
            dataIndex: "billingMultiplier",
            key: "billingMultiplier",
            width: 120,
            render: function (_: unknown, row: AiUserGroupEntity): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.billingMultiplier}x</span>
            }
        },
        {
            title: t('pages.aiUserGroupManager.modal.enabled.label'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: AiUserGroupEntity): React.ReactNode | JSX.Element {
                return row.enabled
                    ? <Tag color="green">{t('pages.aiUserGroupManager.modal.enabled.enabled')}</Tag>
                    : <Tag color="red">{t('pages.aiUserGroupManager.modal.enabled.disabled')}</Tag>
            }
        },
        {
            title: t('pages.aiUserGroupManager.modal.sort.label'),
            dataIndex: "sort",
            key: "sort",
            width: 80,
            render: function (_: unknown, row: AiUserGroupEntity): React.ReactNode | JSX.Element {
                return <span>{row.sort}</span>
            }
        }
    ];
}
