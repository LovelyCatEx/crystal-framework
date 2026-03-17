import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    TenantTireTypeManagerController,
    type ManagerCreateTenantTireTypeDTO,
    type ManagerReadTenantTireTypeDTO
} from "@/api/tenant-tire-type.api.ts";
import {useRef} from "react";
import {useTenantTireTypeTableColumns} from "@/components/columns/TenantTireTypeEntityColumns.tsx";
import {useTranslation} from "react-i18next";

export function TenantTireTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useTenantTireTypeTableColumns();

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.tenantTireType')}
            title={t('pages.tenantTireTypeManager.title')}
            subtitle={t('pages.tenantTireTypeManager.subtitle')}
            columns={columns}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label={t('pages.tenantTireTypeManager.modal.name.label')} rules={[{ required: true, message: t('pages.tenantTireTypeManager.modal.name.required') }, { max: 32, message: t('pages.tenantTireTypeManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireTypeManager.modal.name.placeholder')} maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.tenantTireTypeManager.modal.description.label')} rules={[{ max: 512, message: t('pages.tenantTireTypeManager.modal.description.maxLength') }]}>
                        <Input.TextArea 
                            className="w-full rounded-lg" 
                            placeholder={t('pages.tenantTireTypeManager.modal.description.placeholder')} 
                            rows={4}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadTenantTireTypeDTO) => {
                return (await TenantTireTypeManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantTireTypeManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await TenantTireTypeManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await TenantTireTypeManagerController.create(props as ManagerCreateTenantTireTypeDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
