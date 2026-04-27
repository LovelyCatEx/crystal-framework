import { Col, Form, Input, Row, Select, Switch, Tag, message } from "antd";
import { ManagerPageContainer, type ManagerPageContainerRef } from "@/components/ManagerPageContainer.tsx";
import {
    MailTemplateManagerController,
    type ManagerCreateMailTemplateDTO,
    type ManagerReadMailTemplateDTO
} from "@/api/mail-template.api.ts";
import { useEffect, useRef, useState } from "react";
import type { MailTemplateType, MailTemplate } from "@/types/mail.types.ts";
import { useMailTemplateTableColumns } from "@/components/columns/MailTemplateEntityColumns.tsx";
import { MailTemplateTypeManagerController } from "@/api/mail-template-type.api.ts";
import { HtmlEditor } from "@/components/HtmlEditor.tsx";
import { CopyOutlined } from "@ant-design/icons";
import type { EntityTableColumns } from "@/components/types/entity-table.types.ts";
import {useTranslation} from "react-i18next";

interface TemplateVariablesProps {
    templateType: MailTemplateType | null;
    t: (key: string, options?: Record<string, unknown>) => string;
}

function TemplateVariables({ templateType, t }: TemplateVariablesProps) {
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
            void message.success(t('pages.mailTemplateManager.messages.copySuccess', { variable: `{{${variable}}}` }));
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

interface VariablesDisplayProps {
    templateTypes: MailTemplateType[];
    t: (key: string, options?: Record<string, unknown>) => string;
}

function VariablesDisplay({ templateTypes, t }: VariablesDisplayProps) {
    const form = Form.useFormInstance();
    const typeId = Form.useWatch('typeId', form);
    const selectedType = templateTypes.find((t) => t.id === typeId) || null;

    if (!selectedType) {
        return null;
    }

    return (
        <Form.Item label={t('pages.mailTemplateManager.modal.variables.label')} className="mb-2">
            <TemplateVariables templateType={selectedType} t={t} />
        </Form.Item>
    );
}

export function MailTemplateManagerPage() {
    const { t } = useTranslation();
    const baseColumns = useMailTemplateTableColumns();

    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [templateTypes, setTemplateTypes] = useState<MailTemplateType[]>([]);
    const [selectedTypeId, setSelectedTypeId] = useState<number | null>(null);

    useEffect(() => {
        MailTemplateTypeManagerController.list().then((res) => {
            setTemplateTypes(res.data || []);
        });
    }, []);

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [selectedTypeId]);

    const handleActiveChange = (active: boolean, row: MailTemplate) => {
        MailTemplateManagerController
            .update({ id: row.id, active: active })
            .then(() => {
                void message.success(t('pages.mailTemplateManager.messages.statusUpdateSuccess'));
                pageRef.current?.refreshData();
            })
            .catch(() => {
                void message.error(t('pages.mailTemplateManager.messages.statusUpdateFailed'));
            });
    };

    const columnsWithActive: EntityTableColumns<MailTemplate> = [...baseColumns];
    const statusColumnIndex = columnsWithActive.findIndex(col => col.key === 'active');
    if (statusColumnIndex !== -1) {
        columnsWithActive.splice(statusColumnIndex, 1);
    }
    columnsWithActive.push({
        title: t('pages.mailTemplateManager.enabledStatus'),
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
            entityName={t('entityNames.mailTemplate')}
            title={t('pages.mailTemplateManager.title')}
            subtitle={t('pages.mailTemplateManager.subtitle')}
            columns={columnsWithActive}
            tableActions={[
                {
                    label: t('pages.mailTemplateManager.filter.templateType'),
                    children: (
                        <Select
                            className="w-64"
                            placeholder={t('pages.mailTemplateManager.filter.placeholder')}
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
                            <Form.Item name="name" label={t('pages.mailTemplateManager.modal.name.label')} rules={[{ required: true, message: t('pages.mailTemplateManager.modal.name.required') }, { max: 128, message: t('pages.mailTemplateManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10" placeholder={t('pages.mailTemplateManager.modal.name.placeholder')} maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="typeId" label={t('pages.mailTemplateManager.modal.typeId.label')} rules={[{ required: true, message: t('pages.mailTemplateManager.modal.typeId.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10"
                                    placeholder={t('pages.mailTemplateManager.modal.typeId.placeholder')}
                                    options={templateTypes.map((type) => ({
                                        label: type.name,
                                        value: type.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="title" label={t('pages.mailTemplateManager.modal.title.label')} rules={[{ required: true, message: t('pages.mailTemplateManager.modal.title.required') }, { max: 512, message: t('pages.mailTemplateManager.modal.title.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10" placeholder={t('pages.mailTemplateManager.modal.title.placeholder')} maxLength={512} showCount />
                    </Form.Item>
                    <Form.Item name="description" label={t('pages.mailTemplateManager.modal.description.label')} rules={[{ max: 512, message: t('pages.mailTemplateManager.modal.description.maxLength') }]}>
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder={t('pages.mailTemplateManager.modal.description.placeholder')}
                            rows={3}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                    <VariablesDisplay templateTypes={templateTypes} t={t} />
                    <Form.Item name="content" label={t('pages.mailTemplateManager.modal.content.label')} rules={[{ required: true, message: t('pages.mailTemplateManager.modal.content.required') }]}>
                        <HtmlEditor
                            placeholder={t('pages.mailTemplateManager.modal.content.placeholder')}
                            height={400}
                        />
                    </Form.Item>
                    <Form.Item name="active" label={t('pages.mailTemplateManager.modal.active.label')} valuePropName="checked">
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
