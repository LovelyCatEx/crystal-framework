import {Button, Col, Form, Input, Row, Select} from "antd";
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
import {ApprovalEditorButton} from "@/components/approval/ApprovalEditorOverlay.tsx";

export default function ApprovalFlowDefinitionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useApprovalFlowDefinitionTableColumns();

    const statusOptions = [
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DRAFT), value: ApprovalFlowDefinitionStatus.DRAFT},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.PUBLISHED), value: ApprovalFlowDefinitionStatus.PUBLISHED},
        {label: getApprovalFlowDefinitionStatus(ApprovalFlowDefinitionStatus.DISABLED), value: ApprovalFlowDefinitionStatus.DISABLED},
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.approvalFlowDefinitionManager.title')}
                subtitle={t('pages.approvalFlowDefinitionManager.subtitle')}
                titleActions={
                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={handleOpenAddModal}
                    >
                        {t('pages.approvalFlowDefinitionManager.action.addNew')}
                    </Button>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
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
                                    label={t('pages.approvalFlowDefinitionManager.modal.name.label')}
                                    rules={[{required: true, message: t('pages.approvalFlowDefinitionManager.modal.name.required')}]}
                                >
                                    <Input placeholder={t('pages.approvalFlowDefinitionManager.modal.name.placeholder')} maxLength={128} showCount/>
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="status"
                                    label={t('pages.approvalFlowDefinitionManager.modal.status.label')}
                                    initialValue={ApprovalFlowDefinitionStatus.DRAFT}
                                >
                                    <Select options={statusOptions}/>
                                </Form.Item>
                            </Col>
                        </Row>
                        <Form.Item
                            name="description"
                            label={t('pages.approvalFlowDefinitionManager.modal.description.label')}
                        >
                            <Input.TextArea placeholder={t('pages.approvalFlowDefinitionManager.modal.description.placeholder')} maxLength={512} rows={3}/>
                        </Form.Item>
                    </>
                }
                query={async (props) => {
                    return (await ApprovalFlowDefinitionManagerController.query({
                        ...props,
                        scope: ResourceScope.SYSTEM,
                        scopeId: '0',
                    })).data!;
                }}
                create={async (props) => {
                    await ApprovalFlowDefinitionManagerController.create({
                        ...props,
                        scope: ResourceScope.SYSTEM,
                        scopeId: '0',
                    } as unknown as ManagerCreateApprovalFlowDefinitionDTO);
                }}
                update={async (props) => {
                    await ApprovalFlowDefinitionManagerController.update(props as ManagerUpdateApprovalFlowDefinitionDTO);
                }}
                delete={async (props) => {
                    await ApprovalFlowDefinitionManagerController.delete(props);
                }}
            />
        </>
    );
}
