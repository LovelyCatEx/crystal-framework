import React, {type JSX} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {MailTemplateCategory, MailTemplateType} from "../../types/mail.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "../../compositions/swr.ts";
import {MailTemplateCategoryManagerController} from "../../api/mail-template-category.api.ts";

function CategoryCell({ categoryId }: { categoryId: string }) {
    const { data: category, isLoading } = useSWRComposition<MailTemplateCategory | null>(
        `mail-template-category-${categoryId}`,
        async () => {
            return await MailTemplateCategoryManagerController.getById(categoryId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (category) {
        return <Space orientation='vertical' size={0}>
            <CopyableToolTip title={category.name}>
                <span className="text-xs font-mono font-bold">{category.name}</span>
            </CopyableToolTip>
            <CopyableToolTip title={category.id}>
                <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {category.id}</Tag>
            </CopyableToolTip>
        </Space>;
    }

    return <CopyableToolTip title={categoryId}>
        <Tag color="purple" className="text-xs font-mono">{categoryId}</Tag>
    </CopyableToolTip>;
}

export const MAIL_TEMPLATE_TYPE_MANAGER_TABLE_COLUMNS: EntityTableColumns<MailTemplateType> = [
    {
        title: "名称",
        dataIndex: "name",
        key: "name",
        render: function (_: unknown, row: MailTemplateType): React.ReactNode | JSX.Element {
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
        title: "描述",
        dataIndex: "description",
        key: "description",
        width: 200,
        render: function (_: unknown, row: MailTemplateType): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.description ?? '无描述'}>
                    <span className="text-xs font-mono">{row.description ?? '-'}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "变量",
        dataIndex: "variables",
        key: "variables",
        render: function (_: unknown, row: MailTemplateType): React.ReactNode | JSX.Element {
            return <div className="text-xs font-mono text-gray-500 flex flex-row flex-wrap gap-2 items-start">
                {(JSON.parse(row.variables) as string[]).map((variable) => (
                    <CopyableToolTip title={variable} key={variable}>
                        <Tag>{variable}</Tag>
                    </CopyableToolTip>
                ))}
            </div>

        }
    },
    {
        title: "分类",
        dataIndex: "categoryId",
        key: "categoryId",
        render: function (_: unknown, row: MailTemplateType): React.ReactNode | JSX.Element {
            return <CategoryCell categoryId={row.categoryId} />;
        }
    },
    {
        title: "允许多模板",
        dataIndex: "allowMultiple",
        key: "allowMultiple",
        render: function (_: unknown, row: MailTemplateType): React.ReactNode | JSX.Element {
            return <Tag color={row.allowMultiple ? "green" : "orange"} className="text-xs font-mono">
                {row.allowMultiple ? "是" : "否"}
            </Tag>
        }
    }
];
