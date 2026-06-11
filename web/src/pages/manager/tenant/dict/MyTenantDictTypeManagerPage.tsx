import {Button, Col, Form, Input, Row, Segmented, Select, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantDictTypeDTO,
    type ManagerUpdateTenantDictTypeDTO,
    TenantDictTypeManagerController
} from "@/api/tenant/tenant-dict-type.api.ts";
import {DictTypeStatus} from "@/types/tenant/tenant-dict-type.types.ts";
import {getDictTypeStatus} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef, useState} from "react";
import {useTenantDictTypeTableColumns} from "@/components/columns/TenantDictTypeEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {BookOutlined, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useNavigate} from "react-router-dom";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";

type DictScope = 'myTenant' | 'system';

export default function MyTenantDictTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [scope, setScope] = useState<DictScope>('myTenant');
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const {t} = useTranslation();
    const navigate = useNavigate();
    const columns = useTenantDictTypeTableColumns();

    const currentTenantId = currentTenant?.tenantId ?? null;
    const queryTenantId = scope === 'system' ? '0' : (currentTenantId || '');
    const isReadonly = scope === 'system';

    useEffect(() => {
        if (queryTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [queryTenantId]);

    const statusOptions = [
        {label: getDictTypeStatus(DictTypeStatus.ENABLED), value: DictTypeStatus.ENABLED},
        {label: getDictTypeStatus(DictTypeStatus.DISABLED), value: DictTypeStatus.DISABLED},
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const handleManageItems = (row: TenantDictType) => {
        navigate(`/manager/tenant/dict-items?typeId=${row.id}`);
    };

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantDictTypeManager.title')} subtitle={t('pages.myTenantDictTypeManager.subtitle')}/>
                <div className="flex justify-center items-center h-64">
                    <Spin size="large"/>
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantDictTypeManager.title')}
                subtitle={t('pages.myTenantDictTypeManager.subtitle')}
                titleActions={
                    !isReadonly && currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.myTenantDictTypeManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            <Segmented
                className="mb-4"
                value={scope}
                onChange={(val) => setScope(val as DictScope)}
                options={[
                    {label: t('pages.myTenantDictTypeManager.scope.myTenant'), value: 'myTenant'},
                    {label: t('pages.myTenantDictTypeManager.scope.system'), value: 'system'},
                ]}
            />
            {queryTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    entityName={t('entityNames.tenantDictType')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode={isReadonly}
                    columns={columns}
                    searchKeywords={['code', 'name']}
                    tableRowActionsRender={(row: TenantDictType) => (
                        <Button
                            size="small"
                            icon={<BookOutlined/>}
                            onClick={() => handleManageItems(row)}
                        >
                            {t('pages.myTenantDictTypeManager.action.manageItems')}
                        </Button>
                    )}
                    editModalFormChildren={
                        <>
                            <Form.Item name="tenantId" hidden initialValue={currentTenantId}>
                                <Input/>
                            </Form.Item>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="code"
                                        label={t('pages.myTenantDictTypeManager.modal.code.label')}
                                        rules={[{required: true, message: t('pages.myTenantDictTypeManager.modal.code.required')}]}
                                    >
                                        <Input placeholder={t('pages.myTenantDictTypeManager.modal.code.placeholder')} maxLength={64} showCount/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.myTenantDictTypeManager.modal.name.label')}
                                        rules={[{required: true, message: t('pages.myTenantDictTypeManager.modal.name.required')}]}
                                    >
                                        <Input placeholder={t('pages.myTenantDictTypeManager.modal.name.placeholder')} maxLength={128} showCount/>
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label={t('pages.myTenantDictTypeManager.modal.status.label')}
                                        initialValue={DictTypeStatus.ENABLED}
                                    >
                                        <Select options={statusOptions}/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="remark"
                                        label={t('pages.myTenantDictTypeManager.modal.remark.label')}
                                    >
                                        <Input placeholder={t('pages.myTenantDictTypeManager.modal.remark.placeholder')} maxLength={512}/>
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    }
                    query={async (props) => {
                        return (await TenantDictTypeManagerController.query({
                            ...props,
                            tenantId: queryTenantId
                        })).data!;
                    }}
                    create={async (props) => {
                        await TenantDictTypeManagerController.create({
                            ...props,
                            tenantId: currentTenantId!
                        } as unknown as ManagerCreateTenantDictTypeDTO);
                    }}
                    update={async (props) => {
                        await TenantDictTypeManagerController.update(props as ManagerUpdateTenantDictTypeDTO);
                    }}
                    delete={async (props) => {
                        await TenantDictTypeManagerController.delete(props);
                    }}
                />
            )}
        </>
    );
}
