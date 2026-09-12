import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiProviderEntity} from "@/types/ai/ai.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";
import {getAiProviderProtocolType} from "@/i18n/enum-helpers.ts";

export function useAiProviderTableColumns(): EntityTableColumns<AiProviderEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('pages.aiProviderManager.modal.name.label'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: AiProviderEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <span className="text-sm font-medium">{row.name}</span>
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
            title: t('pages.aiProviderManager.modal.protocolType.label'),
            dataIndex: "protocolType",
            key: "protocolType",
            width: 180,
            render: function (_: unknown, row: AiProviderEntity): React.ReactNode | JSX.Element {
                return <Tag color="blue">{getAiProviderProtocolType(row.protocolType)}</Tag>
            }
        },
        {
            title: t('pages.aiProviderManager.modal.baseUrl.label'),
            dataIndex: "baseUrl",
            key: "baseUrl",
            render: function (_: unknown, row: AiProviderEntity): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.baseUrl}>
                    <span className="text-xs font-mono">{row.baseUrl}</span>
                </CopyableToolTip>
            }
        },
        {
            title: t('pages.aiProviderManager.modal.enabled.label'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: AiProviderEntity): React.ReactNode | JSX.Element {
                return row.enabled
                    ? <Tag color="green">{t('pages.aiProviderManager.modal.enabled.enabled')}</Tag>
                    : <Tag color="red">{t('pages.aiProviderManager.modal.enabled.disabled')}</Tag>
            }
        },
        {
            title: t('pages.aiProviderManager.modal.sort.label'),
            dataIndex: "sort",
            key: "sort",
            width: 80,
            render: function (_: unknown, row: AiProviderEntity): React.ReactNode | JSX.Element {
                return <span>{row.sort}</span>
            }
        }
    ];
}
