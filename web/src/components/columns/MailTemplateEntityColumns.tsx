import React, {type JSX} from "react";
import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {MailTemplate, MailTemplateType} from "@/types/mail.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {MailTemplateTypeCard} from "@/components/card/pop";
import {useSWRComposition} from "@/compositions/swr.ts";
import {MailTemplateTypeManagerController} from "@/api/mail-template-type.api.ts";

function TypeCell({ typeId }: { typeId: string }) {
    const { data: templateType, isLoading } = useSWRComposition<MailTemplateType | null>(
        `mail-template-type-${typeId}`,
        async () => {
            return await MailTemplateTypeManagerController.getById(typeId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (templateType) {
        return (
            <Popover
                content={<MailTemplateTypeCard typeId={typeId} />}
                trigger="hover"
                placement="right"
            >
                <Space orientation="vertical" size={0} className="cursor-pointer hover:opacity-80">
                    <span className="text-xs font-mono font-bold">{templateType.name}</span>
                    <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                        ID: {templateType.id}
                    </Tag>
                </Space>
            </Popover>
        );
    }

    return (
        <Popover
            content={<MailTemplateTypeCard typeId={typeId} />}
            trigger="hover"
            placement="right"
        >
            <Tag color="purple" className="text-xs font-mono cursor-pointer hover:opacity-80">
                {typeId}
            </Tag>
        </Popover>
    );
}

export const MAIL_TEMPLATE_MANAGER_TABLE_COLUMNS: EntityTableColumns<MailTemplate> = [
    {
        title: "名称",
        dataIndex: "name",
        key: "name",
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return (
                <Space orientation="vertical" size={0}>
                    <CopyableToolTip title={row.name}>
                        <span className="text-xs font-mono font-bold">{row.name}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                            ID: {row.id}
                        </Tag>
                    </CopyableToolTip>
                </Space>
            );
        }
    },
    {
        title: "类型",
        dataIndex: "typeId",
        key: "typeId",
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return <TypeCell typeId={row.typeId} />;
        }
    },
    {
        title: "标题",
        dataIndex: "title",
        key: "title",
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return (
                <Space orientation="vertical" size={0}>
                    <CopyableToolTip title={row.title}>
                        <span className="text-xs font-mono font-bold">{row.title}</span>
                    </CopyableToolTip>
                </Space>
            );
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        width: 180,
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return (
                <Space orientation="vertical" size={0}>
                    <CopyableToolTip title={row.description ?? "无描述"}>
                        <span className="text-xs font-mono">{row.description ?? "-"}</span>
                    </CopyableToolTip>
                </Space>
            );
        }
    },
    {
        title: "内容",
        dataIndex: "content",
        key: "content",
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return (
                <Popover
                    content={
                        <iframe
                            srcDoc={row.content}
                            className="w-96 h-64 border rounded bg-white"
                            sandbox="allow-same-origin"
                            title="HTML Preview"
                        />
                    }
                    trigger="hover"
                    placement="bottom"
                >
                    <span className="text-xs font-mono text-gray-500 cursor-pointer hover:text-blue-500">
                        {row.content.substring(0, 32)}...
                    </span>
                </Popover>
            );
        }
    },
    {
        title: "状态",
        dataIndex: "active",
        key: "active",
        render: function (_: unknown, row: MailTemplate): React.ReactNode | JSX.Element {
            return (
                <Tag color={row.active ? "green" : "red"} className="text-xs font-mono">
                    {row.active ? "启用" : "禁用"}
                </Tag>
            );
        }
    }
];
