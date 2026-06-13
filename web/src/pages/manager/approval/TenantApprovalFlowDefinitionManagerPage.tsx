import {Button, Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateApprovalFlowDefinitionDTO,
    type ManagerUpdateApprovalFlowDefinitionDTO,
    ApprovalFlowDefinitionManagerController
} from "@/api/approval/approval-flow-definition.api.ts";
import {ApprovalFlowDefinitionStatus, ResourceScope} from "@/types/approval/approval-flow-definition.types.ts";
import {getApprovalFlowDefinitionStatus} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef, useState} from "react";
import {useApprovalFlowDefinitionTableColumns} from "@/components/columns/ApprovalFlowDefinitionEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ApprovalEditorButton} from "@/components/approval/ApprovalEditorOverlay.tsx";

export default function TenantApprovalFlowDefinitionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
    const columns = useApprovalFlowDefinitionTableColumns();

    useEffect(() => {
        if (selectedTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [selectedTenantId]);

    const statusOptions = [
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DRAFT), value: ApprovalFlowDefinitionStatus.DRAFT},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.PUBLISHED), value: ApprovalFlowDefinitionStatus.PUBLISHED},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DISABLED), value: ApprovalFlowDefinitionStatus.DISABLED},
    ];

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantApprovalFlowDefinitionManager.title')}
                subtitle={t('pages.tenantApprovalFlowDefinitionManager.subtitle')}
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.tenantApprovalFlowDefinitionManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.approvalFlowDefinition')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    searchKeywords={['name']}
                    tableRowActionsRender={(record) => (
                        <ApprovalEditorButton definitionId={record.id} />
                    )}
                    editModalFormChildren={
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.tenantApprovalFlowDefinitionManager.modal.name.label')}
                                        rules={[{required: true, message: t('pages.tenantApprovalFlowDefinitionManager.modal.name.required')}]}
                                    >
                                        <Input placeholder={t('pages.tenantApprovalFlowDefinitionManager.modal.name.placeholder')} maxLength={128} showCount/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label={t('pages.tenantApprovalFlowDefinitionManager.modal.status.label')}
                                        initialValue={ApprovalFlowDefinitionStatus.DRAFT}
                                    >
                                        <Select options={statusOptions}/>
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Form.Item
                                name="description"
                                label={t('pages.tenantApprovalFlowDefinitionManager.modal.description.label')}
                            >
                                <Input.TextArea placeholder={t('pages.tenantApprovalFlowDefinitionManager.modal.description.placeholder')} maxLength={512} rows={3}/>
                            </Form.Item>
                        </>
                    }
                    query={async (props) => {
                        return (await ApprovalFlowDefinitionManagerController.query({
                            ...props,
                            scope: ResourceScope.TENANT,
                            scopeId: selectedTenantId,
                        })).data!;
                    }}
                    create={async (props) => {
                        await ApprovalFlowDefinitionManagerController.create({
                            ...props,
                            scope: ResourceScope.TENANT,
                            scopeId: selectedTenantId,
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
