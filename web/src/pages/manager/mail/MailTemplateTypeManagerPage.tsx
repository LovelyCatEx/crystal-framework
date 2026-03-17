import {Col, Form, Input, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateMailTemplateTypeDTO,
    type ManagerReadMailTemplateTypeDTO
} from "@/api/mail-template-type.api.ts";
import {useEffect, useRef, useState} from "react";
import type {MailTemplateCategory, MailTemplateType} from "@/types/mail.types.ts";
import {useMailTemplateTypeTableColumns} from "@/components/columns/MailTemplateTypeEntityColumns.tsx";
import {MailTemplateCategoryManagerController} from "@/api/mail-template-category.api.ts";
import {JsonEditor} from "@/components/JsonEditor.tsx";
import {useProtectedController} from "@/components/ProtectedControllerWarningWrapper.tsx";
import {useTranslation} from "react-i18next";

export function MailTemplateTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [categories, setCategories] = useState<MailTemplateCategory[]>([]);
    const { controller } = useProtectedController<MailTemplateType, ManagerCreateMailTemplateTypeDTO, ManagerReadMailTemplateTypeDTO>();
    const {t} = useTranslation();
    const columns = useMailTemplateTypeTableColumns();

    useEffect(() => {
        MailTemplateCategoryManagerController.list().then((res) => {
            setCategories(res.data || []);
        });
    }, []);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.mailTemplateType')}
            title={t('pages.mailTemplateTypeManager.title')}
            subtitle={t('pages.mailTemplateTypeManager.subtitle')}
            columns={columns}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label={t('pages.mailTemplateTypeManager.modal.name.label')} rules={[{ required: true, message: t('pages.mailTemplateTypeManager.modal.name.required') }, { max: 128, message: t('pages.mailTemplateTypeManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.mailTemplateTypeManager.modal.name.placeholder')} maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="categoryId" label={t('pages.mailTemplateTypeManager.modal.categoryId.label')} rules={[{ required: true, message: t('pages.mailTemplateTypeManager.modal.categoryId.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.mailTemplateTypeManager.modal.categoryId.placeholder')}
                                    options={categories.map((cat) => ({
                                        label: cat.name,
                                        value: cat.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.mailTemplateTypeManager.modal.description.label')} rules={[{ max: 512, message: t('pages.mailTemplateTypeManager.modal.description.maxLength') }]}>
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder={t('pages.mailTemplateTypeManager.modal.description.placeholder')}
                            rows={3}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                    <Form.Item name="variables" label={t('pages.mailTemplateTypeManager.modal.variables.label')} rules={[{ required: true, message: t('pages.mailTemplateTypeManager.modal.variables.required') }]}>
                        <JsonEditor placeholder={t('pages.mailTemplateTypeManager.modal.variables.placeholder')} />
                    </Form.Item>
                    <Form.Item name="allowMultiple" label={t('pages.mailTemplateTypeManager.modal.allowMultiple.label')} valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadMailTemplateTypeDTO) => {
                return (await controller.query(props)).data!
            }}
            delete={async (props) => {
                return (await controller.delete(props)).data!
            }}
            update={async (props) => {
                return (await controller.update(props)).data!
            }}
            create={async (props) => {
                return (await controller.create(props as ManagerCreateMailTemplateTypeDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
