import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {MailTemplateCategory} from "@/types/mail.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useMailTemplateCategoryTableColumns(): EntityTableColumns<MailTemplateCategory> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.mailTemplateCategory.name'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: MailTemplateCategory): React.ReactNode | JSX.Element {
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
            title: t('components.columns.mailTemplateCategory.description'),
            dataIndex: "description",
            key: "description",
            width: 300,
            render: function (_: unknown, row: MailTemplateCategory): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.description ?? '-'}>
                        <span className="text-xs font-mono">{row.description ?? '-'}</span>
                    </CopyableToolTip>
                </Space>
            }
        }
    ];
}
