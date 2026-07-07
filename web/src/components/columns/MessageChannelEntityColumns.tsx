import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {MessageChannel} from "@/types/message-channel/message-channel.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {getChannelType} from "@/i18n/enum-helpers.ts";
import {ChannelType} from "@/types/message-channel/message-channel.types.ts";
import {useTranslation} from "react-i18next";

const CHANNEL_TYPE_COLORS: Record<number, string> = {
    [ChannelType.EMAIL]: 'green',
    [ChannelType.LARK]: 'blue'
};

export function useMessageChannelTableColumns(): EntityTableColumns<MessageChannel> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.messageChannel.recordInfo'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: MessageChannel): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.scopeId}>
                        <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.messageChannel.scopeId')}: {row.scopeId}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.messageChannel.name'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: MessageChannel): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.name}>
                    <span className="text-xs font-mono font-bold">{row.name}</span>
                </CopyableToolTip>
            }
        },
        {
            title: t('components.columns.messageChannel.channelType'),
            dataIndex: "channelType",
            key: "channelType",
            render: function (_: unknown, row: MessageChannel): React.ReactNode | JSX.Element {
                return <Tag color={CHANNEL_TYPE_COLORS[row.channelType] || 'default'} className="text-xs font-mono">
                    {getChannelType(row.channelType)}
                </Tag>
            }
        },
        {
            title: t('components.columns.messageChannel.config'),
            dataIndex: "config",
            key: "config",
            render: function (_: unknown, row: MessageChannel): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.config}>
                    <span className="text-xs font-mono text-gray-500">{row.config.substring(0, 32)}...</span>
                </CopyableToolTip>
            }
        }
    ];
}
