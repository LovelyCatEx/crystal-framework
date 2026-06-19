import {Button, Col, Form, Input, InputNumber, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantDictItemDTO,
    type ManagerUpdateTenantDictItemDTO,
    TenantDictItemManagerController
} from "@/api/tenant/tenant-dict-item.api.ts";
import {DictItemStatus} from "@/types/tenant/tenant-dict-item.types.ts";
import {getDictItemStatus} from "@/i18n/enum-helpers.ts";
import {useRef} from "react";
import {useTenantDictItemTableColumns} from "@/components/columns/TenantDictItemEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ArrowLeftOutlined, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useNavigate, useSearchParams} from "react-router-dom";

export default function SystemDictItemManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [searchParams] = useSearchParams();
    const typeId = searchParams.get('typeId') || '';
    const {t} = useTranslation();
    const navigate = useNavigate();
    const columns = useTenantDictItemTableColumns();

    const statusOptions = [
        {label: getDictItemStatus(DictItemStatus.ENABLED), value: DictItemStatus.ENABLED},
        {label: getDictItemStatus(DictItemStatus.DISABLED), value: DictItemStatus.DISABLED},
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const handleBack = () => {
        navigate('/manager/system-dict-types');
    };

    if (!typeId) {
        return (
            <div className="text-center py-12 text-gray-400">
                {t('pages.tenantDictItemManager.noTypeSelected')}
            </div>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.systemDictItemManager.title')}
                subtitle={t('pages.systemDictItemManager.subtitle')}
                titleActions={
                    <div className="flex gap-3">
                        <Button
                            icon={<ArrowLeftOutlined/>}
                            size="large"
                            className="rounded-xl h-12"
                            onClick={handleBack}
                        >
                            {t('pages.systemDictItemManager.action.back')}
                        </Button>
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.systemDictItemManager.action.addNew')}
                        </Button>
                    </div>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
                className="mt-4"
                entityName={t('entityNames.tenantDictItem')}
                title=""
                subtitle=""
                showActionBar={false}
                columns={columns}
                searchKeywords={['item_code', 'item_value']}
                editModalFormChildren={
                    <>
                        <Form.Item name="typeId" hidden initialValue={typeId}>
                            <Input/>
                        </Form.Item>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="itemCode"
                                    label={t('pages.tenantDictItemManager.modal.itemCode.label')}
                                    rules={[{required: true, message: t('pages.tenantDictItemManager.modal.itemCode.required')}]}
                                >
                                    <Input placeholder={t('pages.tenantDictItemManager.modal.itemCode.placeholder')} maxLength={64} showCount/>
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="itemValue"
                                    label={t('pages.tenantDictItemManager.modal.itemValue.label')}
                                    rules={[{required: true, message: t('pages.tenantDictItemManager.modal.itemValue.required')}]}
                                >
                                    <Input placeholder={t('pages.tenantDictItemManager.modal.itemValue.placeholder')} maxLength={256} showCount/>
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={8}>
                                <Form.Item
                                    name="sortOrder"
                                    label={t('pages.tenantDictItemManager.modal.sortOrder.label')}
                                    initialValue={0}
                                >
                                    <InputNumber className="w-full" min={0}/>
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Form.Item
                                    name="status"
                                    label={t('pages.tenantDictItemManager.modal.status.label')}
                                    initialValue={DictItemStatus.ENABLED}
                                >
                                    <Select options={statusOptions}/>
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Form.Item
                                    name="isDefault"
                                    label={t('pages.tenantDictItemManager.modal.isDefault.label')}
                                    valuePropName="checked"
                                    initialValue={false}
                                >
                                    <Switch/>
                                </Form.Item>
                            </Col>
                        </Row>
                    </>
                }
                query={async (props) => {
                    return (await TenantDictItemManagerController.query({
                        ...props,
                        typeId
                    })).data!;
                }}
                create={async (props) => {
                    await TenantDictItemManagerController.create({
                        ...props,
                        typeId
                    } as unknown as ManagerCreateTenantDictItemDTO);
                }}
                update={async (props) => {
                    await TenantDictItemManagerController.update(props as ManagerUpdateTenantDictItemDTO);
                }}
                delete={async (props) => {
                    await TenantDictItemManagerController.delete(props);
                }}
            />
        </>
    );
}
