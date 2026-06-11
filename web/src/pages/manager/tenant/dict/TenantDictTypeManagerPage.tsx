import {Button, Col, Form, Input, Row, Segmented, Select} from "antd";
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
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {BookOutlined, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useNavigate} from "react-router-dom";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";

type DictScope = 'system' | 'tenant';

export default function TenantDictTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [scope, setScope] = useState<DictScope>('system');
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
    const navigate = useNavigate();
    const columns = useTenantDictTypeTableColumns();

    const currentTenantId = scope === 'system' ? '0' : (selectedTenantId || '');

    useEffect(() => {
        if (currentTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [currentTenantId]);

    const statusOptions = [
        {label: getDictTypeStatus(DictTypeStatus.ENABLED), value: DictTypeStatus.ENABLED},
        {label: getDictTypeStatus(DictTypeStatus.DISABLED), value: DictTypeStatus.DISABLED},
    ];

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const handleManageItems = (row: TenantDictType) => {
        navigate(`/manager/tenant-dict-items?typeId=${row.id}&tenantId=${currentTenantId}`);
    };

    const canQuery = scope === 'system' || !!selectedTenantId;

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantDictTypeManager.title')}
                subtitle={t('pages.tenantDictTypeManager.subtitle')}
                titleActions={
                    canQuery ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.tenantDictTypeManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            <Segmented
                className="mb-4"
                value={scope}
                onChange={(val) => setScope(val as DictScope)}
                options={[
                    {label: t('pages.tenantDictTypeManager.scope.system'), value: 'system'},
                    {label: t('pages.tenantDictTypeManager.scope.tenant'), value: 'tenant'},
                ]}
            />
            {scope === 'tenant' && (
                <TenantSelectorWithDetail
                    value={selectedTenantId}
                    onChange={handleTenantChange}
                />
            )}
            {canQuery && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
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
                            {t('pages.tenantDictTypeManager.action.manageItems')}
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
                                        label={t('pages.tenantDictTypeManager.modal.code.label')}
                                        rules={[{required: true, message: t('pages.tenantDictTypeManager.modal.code.required')}]}
                                    >
                                        <Input placeholder={t('pages.tenantDictTypeManager.modal.code.placeholder')} maxLength={64} showCount/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.tenantDictTypeManager.modal.name.label')}
                                        rules={[{required: true, message: t('pages.tenantDictTypeManager.modal.name.required')}]}
                                    >
                                        <Input placeholder={t('pages.tenantDictTypeManager.modal.name.placeholder')} maxLength={128} showCount/>
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label={t('pages.tenantDictTypeManager.modal.status.label')}
                                        initialValue={DictTypeStatus.ENABLED}
                                    >
                                        <Select options={statusOptions}/>
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="remark"
                                        label={t('pages.tenantDictTypeManager.modal.remark.label')}
                                    >
                                        <Input placeholder={t('pages.tenantDictTypeManager.modal.remark.placeholder')} maxLength={512}/>
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    }
                    query={async (props) => {
                        return (await TenantDictTypeManagerController.query({
                            ...props,
                            tenantId: currentTenantId
                        })).data!;
                    }}
                    create={async (props) => {
                        await TenantDictTypeManagerController.create({
                            ...props,
                            tenantId: currentTenantId
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
