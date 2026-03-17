import {Button, Col, Form, Input, Row, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantRoleDTO,
    type ManagerUpdateTenantRoleDTO,
    TenantRoleManagerController
} from "@/api/tenant-role.api.ts";
import {useRef} from "react";
import {useTenantRoleTableColumns} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {TenantRoleIdSelector} from "@/components/selector/TenantRoleIdSelector.tsx";
import {PlusOutlined} from "@ant-design/icons";
import type {TenantRole} from "@/types/tenat-role.types.ts";
import {useTranslation} from "react-i18next";

export function MyTenantRoleManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const currentTenantId = currentTenant?.tenantId ?? null;
    const {t} = useTranslation();
    const columns = useTenantRoleTableColumns();

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantRoleManager.title')} subtitle={t('pages.myTenantRoleManager.subtitle')} />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantRoleManager.title')}
                subtitle={t('pages.myTenantRoleManager.subtitle')}
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.myTenantRoleManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.tenantRole')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    editModalFormChildren={(editingItem: TenantRole | null) => (
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={currentTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.myTenantRoleManager.modal.name.label')}
                                        rules={[{ required: true, message: t('pages.myTenantRoleManager.modal.name.required') }]}
                                    >
                                        <Input
                                            className="w-full rounded-lg h-10"
                                            placeholder={t('pages.myTenantRoleManager.modal.name.placeholder')}
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="parentId"
                                        label={t('pages.myTenantRoleManager.modal.parentId.label')}
                                    >
                                        <TenantRoleIdSelector tenantId={currentTenantId || ''} disabledRoleId={editingItem?.id ?? null} />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={24}>
                                    <Form.Item
                                        name="description"
                                        label={t('pages.myTenantRoleManager.modal.description.label')}
                                    >
                                        <Input.TextArea
                                            className="w-full rounded-lg"
                                            placeholder={t('pages.myTenantRoleManager.modal.description.placeholder')}
                                            maxLength={512}
                                            showCount
                                            rows={2}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    )}
                    query={async (props) => {
                        return (await TenantRoleManagerController.query({
                            ...props,
                            tenantId: currentTenantId
                        })).data!
                    }}
                    delete={async (props) => {
                        return (await TenantRoleManagerController.delete(props)).data!
                    }}
                    update={async (props: ManagerUpdateTenantRoleDTO) => {
                        return (await TenantRoleManagerController.update(props)).data!
                    }}
                    create={async (props) => {
                        const createProps: ManagerCreateTenantRoleDTO = {
                            ...(props as ManagerCreateTenantRoleDTO),
                            tenantId: currentTenantId
                        };
                        return (await TenantRoleManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
