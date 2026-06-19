import {Button, Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantDictTypeDTO,
    type ManagerUpdateTenantDictTypeDTO,
    TenantDictTypeManagerController
} from "@/api/tenant/tenant-dict-type.api.ts";
import {DictTypeStatus} from "@/types/tenant/tenant-dict-type.types.ts";
import {ResourceScope} from "@/types/approval/approval-flow-definition.types.ts";
import {getDictTypeStatus} from "@/i18n/enum-helpers.ts";
import {useRef} from "react";
import {useTenantDictTypeTableColumns} from "@/components/columns/TenantDictTypeEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useNavigate} from "react-router-dom";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";
import {BookOutlined} from "@ant-design/icons";

export default function SystemDictTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const navigate = useNavigate();
    const columns = useTenantDictTypeTableColumns();

    const statusOptions = [
        {label: getDictTypeStatus(DictTypeStatus.ENABLED), value: DictTypeStatus.ENABLED},
        {label: getDictTypeStatus(DictTypeStatus.DISABLED), value: DictTypeStatus.DISABLED},
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const handleManageItems = (row: TenantDictType) => {
        navigate(`/manager/system-dict-items?typeId=${row.id}`);
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.systemDictTypeManager.title')}
                subtitle={t('pages.systemDictTypeManager.subtitle')}
                titleActions={
                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={handleOpenAddModal}
                    >
                        {t('pages.systemDictTypeManager.action.addNew')}
                    </Button>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.tenantDictType')}
                title=""
                subtitle=""
                showActionBar={false}
                columns={columns}
                searchKeywords={['code', 'name']}
                tableRowActionsRender={(row: TenantDictType) => (
                    <Button
                        size="small"
                        icon={<BookOutlined/>}
                        onClick={() => handleManageItems(row)}
                    >
                        {t('pages.systemDictTypeManager.action.manageItems')}
                    </Button>
                )}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="code"
                                    label={t('pages.systemDictTypeManager.modal.code.label')}
                                    rules={[{required: true, message: t('pages.systemDictTypeManager.modal.code.required')}]}
                                >
                                    <Input placeholder={t('pages.systemDictTypeManager.modal.code.placeholder')} maxLength={64} showCount/>
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="name"
                                    label={t('pages.systemDictTypeManager.modal.name.label')}
                                    rules={[{required: true, message: t('pages.systemDictTypeManager.modal.name.required')}]}
                                >
                                    <Input placeholder={t('pages.systemDictTypeManager.modal.name.placeholder')} maxLength={128} showCount/>
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="status"
                                    label={t('pages.systemDictTypeManager.modal.status.label')}
                                    initialValue={DictTypeStatus.ENABLED}
                                >
                                    <Select options={statusOptions}/>
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="remark"
                                    label={t('pages.systemDictTypeManager.modal.remark.label')}
                                >
                                    <Input placeholder={t('pages.systemDictTypeManager.modal.remark.placeholder')} maxLength={512}/>
                                </Form.Item>
                            </Col>
                        </Row>
                    </>
                }
                query={async (props) => {
                    return (await TenantDictTypeManagerController.query({
                        ...props,
                        scope: ResourceScope.SYSTEM,
                        scopeId: '0',
                    })).data!;
                }}
                create={async (props) => {
                    await TenantDictTypeManagerController.create({
                        ...props,
                        scope: ResourceScope.SYSTEM,
                        scopeId: '0',
                    } as unknown as ManagerCreateTenantDictTypeDTO);
                }}
                update={async (props) => {
                    await TenantDictTypeManagerController.update(props as ManagerUpdateTenantDictTypeDTO);
                }}
                delete={async (props) => {
                    await TenantDictTypeManagerController.delete(props);
                }}
            />
        </>
    );
}
