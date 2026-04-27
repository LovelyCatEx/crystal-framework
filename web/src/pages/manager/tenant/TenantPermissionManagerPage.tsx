import {Button, Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantPermissionDTO,
    type ManagerUpdateTenantPermissionDTO,
    type ManagerReadTenantPermissionDTO
} from "@/api/tenant-permission.api.ts";
import {TenantPermissionType} from "@/types/tenant-permission.types.ts";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef, useState} from "react";
import {useTenantPermissionTableColumns} from "@/components/columns/TenantPermissionEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useProtectedController} from "@/components/ProtectedControllerWarningWrapper.tsx";
import type {TenantPermission} from "@/types/tenant-permission.types.ts";
import {useTranslation} from "react-i18next";

export function TenantPermissionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>();
    const { controller } = useProtectedController<TenantPermission, ManagerCreateTenantPermissionDTO, ManagerReadTenantPermissionDTO>();
    const {t} = useTranslation();
    const columns = useTenantPermissionTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filterType]);

    const typeOptions = [
        { label: getTenantPermissionType(TenantPermissionType.ACTION), value: TenantPermissionType.ACTION },
        { label: getTenantPermissionType(TenantPermissionType.MENU), value: TenantPermissionType.MENU }
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantPermissionManager.title')}
                subtitle={t('pages.tenantPermissionManager.subtitle')}
                titleActions={
                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={handleOpenAddModal}
                    >
                        {t('pages.tenantPermissionManager.action.addNew')}
                    </Button>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
                className="mt-4"
                entityName={t('entityNames.tenantPermission')}
                title=""
                subtitle=""
                showActionBar={false}
                columns={columns}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="name"
                                    label={t('pages.tenantPermissionManager.modal.name.label')}
                                    rules={[{ required: true, message: t('pages.tenantPermissionManager.modal.name.required') }]}
                                >
                                    <Input
                                        className="w-full rounded-lg h-10"
                                        placeholder={t('pages.tenantPermissionManager.modal.name.placeholder')}
                                        maxLength={256}
                                        showCount
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="type"
                                    label={t('pages.tenantPermissionManager.modal.type.label')}
                                    rules={[{ required: true, message: t('pages.tenantPermissionManager.modal.type.required') }]}
                                    initialValue={TenantPermissionType.ACTION}
                                >
                                    <Select
                                        className="w-full rounded-lg h-10 flex items-center"
                                        placeholder={t('pages.tenantPermissionManager.modal.type.placeholder')}
                                        options={typeOptions}
                                    />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="path"
                                    label={t('pages.tenantPermissionManager.modal.path.label')}
                                >
                                    <Input
                                        className="w-full rounded-lg h-10"
                                        placeholder={t('pages.tenantPermissionManager.modal.path.placeholder')}
                                        maxLength={256}
                                        showCount
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="description"
                                    label={t('pages.tenantPermissionManager.modal.description.label')}
                                >
                                    <Input.TextArea
                                        className="w-full rounded-lg"
                                        placeholder={t('pages.tenantPermissionManager.modal.description.placeholder')}
                                        maxLength={512}
                                        showCount
                                        rows={1}
                                    />
                                </Form.Item>
                            </Col>
                        </Row>
                    </>
                }
                query={async (props) => {
                    return (await controller.query(props)).data!
                }}
                tableActions={[
                    {
                        label: <span>{t('pages.tenantPermissionManager.filter.type')}</span>,
                        children: <Select
                            defaultValue="-1"
                            style={{ width: 120 }}
                            options={[
                                { value: '-1', label: t('pages.tenantPermissionManager.filter.all') },
                                ...typeOptions
                            ]}
                            onChange={(value) => setFilterType(value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                        queryParamsProvider() {
                            return {
                                type: filterType
                            };
                        }
                    }
                ]}
                delete={async (props) => {
                    return (await controller.delete(props)).data!
                }}
                update={async (props: ManagerUpdateTenantPermissionDTO) => {
                    return (await controller.update(props)).data!
                }}
                create={async (props) => {
                    return (await controller.create(props as ManagerCreateTenantPermissionDTO)).data!
                }}
            />
        </>
    )
}
