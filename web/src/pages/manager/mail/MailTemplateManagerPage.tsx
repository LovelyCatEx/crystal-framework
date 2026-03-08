import { Col, Form, Input, Row, Select, Switch, Tag, message } from "antd";
import { ManagerPageContainer, type ManagerPageContainerRef } from "../../../components/ManagerPageContainer.tsx";
import {
    MailTemplateManagerController,
    type ManagerCreateMailTemplateDTO,
    type ManagerReadMailTemplateDTO
} from "../../../api/mail-template.api.ts";
import { useEffect, useRef, useState } from "react";
import type { MailTemplateType, MailTemplate } from "../../../types/mail.types.ts";
import { MAIL_TEMPLATE_MANAGER_TABLE_COLUMNS } from "../../../components/columns/MailTemplateEntityColumns.tsx";
import { MailTemplateTypeManagerController } from "../../../api/mail-template-type.api.ts";
import { HtmlEditor } from "../../../components/HtmlEditor.tsx";
import { CopyOutlined } from "@ant-design/icons";
import type { EntityTableColumns } from "../../../components/types/entity-table.types.ts";

interface TemplateVariablesProps {
    templateType: MailTemplateType | null;
}

function TemplateVariables({ templateType }: TemplateVariablesProps) {
    if (!templateType) {
        return null;
    }

    let variables: string[] = [];
    try {
        const parsed = JSON.parse(templateType.variables);
        if (Array.isArray(parsed)) {
            variables = parsed;
        } else if (typeof parsed === "object" && parsed !== null) {
            variables = Object.keys(parsed);
        }
    } catch {
        return null;
    }

    if (variables.length === 0) {
        return null;
    }

    const handleCopy = (variable: string) => {
        navigator.clipboard.writeText(`{{${variable}}}`).then(() => {
            void message.success(`已复制 {{${variable}}} 到剪切板`);
        });
    };

    return (
        <div className="flex flex-wrap gap-2">
            {variables.map((variable) => (
                <Tag
                    key={variable}
                    color="blue"
                    className="cursor-pointer hover:opacity-80"
                    icon={<CopyOutlined />}
                    onClick={() => handleCopy(variable)}
                >
                    {variable}
                </Tag>
            ))}
        </div>
    );
}

function VariablesDisplay({ templateTypes }: { templateTypes: MailTemplateType[] }) {
    const form = Form.useFormInstance();
    const typeId = Form.useWatch('typeId', form);
    const selectedType = templateTypes.find((t) => t.id === typeId) || null;

    if (!selectedType) {
        return null;
    }

    return (
        <Form.Item label="可用变量" className="mb-2">
            <TemplateVariables templateType={selectedType} />
        </Form.Item>
    );
}

export function MailTemplateManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [templateTypes, setTemplateTypes] = useState<MailTemplateType[]>([]);
    const [selectedTypeId, setSelectedTypeId] = useState<number | null>(null);

    useEffect(() => {
        MailTemplateTypeManagerController.list().then((res) => {
            setTemplateTypes(res.data || []);
        });
    }, []);

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [selectedTypeId]);

    const handleActiveChange = (active: boolean, row: MailTemplate) => {
        MailTemplateManagerController
            .update({ id: row.id, active: active })
            .then(() => {
                void message.success("状态更新成功");
                pageRef.current?.refreshData();
            })
            .catch(() => {
                void message.error("状态更新失败");
            });
    };

    const columnsWithActive: EntityTableColumns<MailTemplate> = [...MAIL_TEMPLATE_MANAGER_TABLE_COLUMNS];
    const statusColumnIndex = columnsWithActive.findIndex(col => col.key === 'active');
    if (statusColumnIndex !== -1) {
        columnsWithActive.splice(statusColumnIndex, 1);
    }
    columnsWithActive.push({
        title: "启用状态",
        dataIndex: "active",
        key: "active",
        width: 100,
        render: function (_: unknown, row: MailTemplate) {
            return <Switch checked={row.active} onChange={(checked) => handleActiveChange(checked, row)} />;
        }
    });

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="邮件模板"
            title="邮件模板管理"
            subtitle="管理邮件模板内容"
            columns={columnsWithActive}
            tableActions={[
                {
                    label: '模板类型',
                    children: (
                        <Select
                            className="w-64"
                            placeholder="选择模板类型"
                            allowClear
                            value={selectedTypeId}
                            onChange={(value) => setSelectedTypeId(value)}
                            options={templateTypes.map((type) => ({
                                label: type.name,
                                value: type.id,
                            }))}
                        />
                    ),
                    queryParamsProvider: () => ({
                        typeId: selectedTypeId ?? undefined,
                    }),
                },
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10" placeholder="模板名称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="typeId" label="类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10"
                                    placeholder="选择类型"
                                    options={templateTypes.map((type) => ({
                                        label: type.name,
                                        value: type.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="title" label="标题" rules={[{ required: true }]}>
                        <Input className="w-full rounded-lg h-10" placeholder="邮件标题" />
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder="模板描述"
                            rows={3}
                        />
                    </Form.Item>
                    <VariablesDisplay templateTypes={templateTypes} />
                    <Form.Item name="content" label="内容" rules={[{ required: true }]}>
                        <HtmlEditor
                            placeholder="邮件模板内容，支持变量替换"
                            height={400}
                        />
                    </Form.Item>
                    <Form.Item name="active" label="启用状态" valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadMailTemplateDTO) => {
                return (await MailTemplateManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await MailTemplateManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await MailTemplateManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await MailTemplateManagerController.create(props as ManagerCreateMailTemplateDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
