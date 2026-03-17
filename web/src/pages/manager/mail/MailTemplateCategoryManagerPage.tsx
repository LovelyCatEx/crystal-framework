import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateMailTemplateCategoryDTO,
    type ManagerReadMailTemplateCategoryDTO
} from "@/api/mail-template-category.api.ts";
import {useRef} from "react";
import {
    useMailTemplateCategoryTableColumns
} from "@/components/columns/MailTemplateCategoryEntityColumns.tsx";
import {useProtectedController} from "@/components/ProtectedControllerWarningWrapper.tsx";
import type {MailTemplateCategory} from "@/types/mail.types.ts";
import {useTranslation} from "react-i18next";

export function MailTemplateCategoryManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { controller } = useProtectedController<MailTemplateCategory, ManagerCreateMailTemplateCategoryDTO, ManagerReadMailTemplateCategoryDTO>();
    const {t} = useTranslation();
    const columns = useMailTemplateCategoryTableColumns();

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.mailTemplateCategory')}
            title={t('pages.mailTemplateCategoryManager.title')}
            subtitle={t('pages.mailTemplateCategoryManager.subtitle')}
            columns={columns}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label={t('pages.mailTemplateCategoryManager.modal.name.label')} rules={[{ required: true, message: t('pages.mailTemplateCategoryManager.modal.name.required') }, { max: 128, message: t('pages.mailTemplateCategoryManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.mailTemplateCategoryManager.modal.name.placeholder')} maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.mailTemplateCategoryManager.modal.description.label')} rules={[{ max: 512, message: t('pages.mailTemplateCategoryManager.modal.description.maxLength') }]}>
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder={t('pages.mailTemplateCategoryManager.modal.description.placeholder')}
                            rows={4}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadMailTemplateCategoryDTO) => {
                return (await controller.query(props)).data!
            }}
            delete={async (props) => {
                return (await controller.delete(props)).data!
            }}
            update={async (props) => {
                return (await controller.update(props)).data!
            }}
            create={async (props) => {
                return (await controller.create(props as ManagerCreateMailTemplateCategoryDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
