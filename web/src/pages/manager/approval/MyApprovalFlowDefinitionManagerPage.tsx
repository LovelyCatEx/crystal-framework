import {Button, Col, Form, Input, Row, Select, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateApprovalFlowDefinitionDTO,
    type ManagerUpdateApprovalFlowDefinitionDTO,
    ApprovalFlowDefinitionManagerController
} from "@/api/approval/approval-flow-definition.api.ts";
import {ApprovalFlowDefinitionStatus, ResourceScope} from "@/types/approval/approval-flow-definition.types.ts";
import {getApprovalFlowDefinitionStatus} from "@/i18n/enum-helpers.ts";
import {useRef} from "react";
import {useApprovalFlowDefinitionTableColumns} from "@/components/columns/ApprovalFlowDefinitionEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useUserTenants} from "@/compositions/use-tenant.ts";

export default function MyApprovalFlowDefinitionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const {t} = useTranslation();
    const columns = useApprovalFlowDefinitionTableColumns();

    const currentTenantId = currentTenant?.tenantId ?? null;

    const statusOptions = [
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DRAFT), value: ApprovalFlowDefinitionStatus.DRAFT},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.PUBLISHED), value: ApprovalFlowDefinitionStatus.PUBLISHED},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DISABLED), value: ApprovalFlowDefinitionStatus.DISABLED},
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myApprovalFlowDefinitionManager.title')} subtitle={t('pages.myApprovalFlowDefinitionManager.subtitle')}/>
                <div className="flex justify-center items-center h-64">
                    <Spin size="large"/>
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myApprovalFlowDefinitionManager.title')}
                subtitle={t('pages.myApprovalFlowDefinitionManager.subtitle')}
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.myApprovalFlowDefinitionManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    entityName={t('entityNames.approvalFlowDefinition')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    searchKeywords={['name']}
                    editModalFormChildren={
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.myApprovalFlowDefinitionManager.modal.name.label')}
                                        rules={[{required: true, message: t('pages.myApprovalFlowDefinitionManager.modal.name.required')}]}
                                    >
                                        <Input placeholder={t('pages.myApprovalFlowDefinitionManager.modal.name.placeholder')} maxLength={128} showCount/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label={t('pages.myApprovalFlowDefinitionManager.modal.status.label')}
                                        initialValue={ApprovalFlowDefinitionStatus.DRAFT}
                                    >
                                        <Select options={statusOptions}/>
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Form.Item
                                name="description"
                                label={t('pages.myApprovalFlowDefinitionManager.modal.description.label')}
                            >
                                <Input.TextArea placeholder={t('pages.myApprovalFlowDefinitionManager.modal.description.placeholder')} maxLength={512} rows={3}/>
                            </Form.Item>
                        </>
                    }
                    query={async (props) => {
                        return (await ApprovalFlowDefinitionManagerController.query({
                            ...props,
                            scope: ResourceScope.TENANT,
                            scopeId: currentTenantId,
                        })).data!;
                    }}
                    create={async (props) => {
                        await ApprovalFlowDefinitionManagerController.create({
                            ...props,
                            scope: ResourceScope.TENANT,
                            scopeId: currentTenantId,
                        } as unknown as ManagerCreateApprovalFlowDefinitionDTO);
                    }}
                    update={async (props) => {
                        await ApprovalFlowDefinitionManagerController.update(props as ManagerUpdateApprovalFlowDefinitionDTO);
                    }}
                    delete={async (props) => {
                        await ApprovalFlowDefinitionManagerController.delete(props);
                    }}
                />
            )}
        </>
    );
}
