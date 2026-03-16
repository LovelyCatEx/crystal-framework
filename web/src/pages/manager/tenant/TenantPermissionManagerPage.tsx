import {Button, Col, Form, Input, InputNumber, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantPermissionDTO,
    type ManagerUpdateTenantPermissionDTO,
    TenantPermissionManagerController,
    TenantPermissionType,
    TenantPermissionTypeMap
} from "@/api/tenant-permission.api.ts";
import {useEffect, useRef, useState} from "react";
import {TENANT_PERMISSION_TABLE_COLUMNS} from "@/components/columns/TenantPermissionEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {PlusOutlined} from "@ant-design/icons";

export function TenantPermissionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>();

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterType]);

    const typeOptions = [
        { label: TenantPermissionTypeMap[TenantPermissionType.ACTION].label, value: TenantPermissionType.ACTION },
        { label: TenantPermissionTypeMap[TenantPermissionType.MENU].label, value: TenantPermissionType.MENU }
    ];

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title="租户权限管理"
                subtitle="管理租户权限信息"
                titleActions={
                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={handleOpenAddModal}
                    >
                        新增权限
                    </Button>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
                className="mt-4"
                entityName="租户权限"
                title=""
                subtitle=""
                showActionBar={false}
                columns={TENANT_PERMISSION_TABLE_COLUMNS}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="name"
                                    label="权限名称"
                                    rules={[{ required: true, message: '请输入权限名称' }]}
                                >
                                    <Input
                                        className="w-full rounded-lg h-10"
                                        placeholder="输入权限名称"
                                        maxLength={256}
                                        showCount
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="type"
                                    label="权限类型"
                                    rules={[{ required: true, message: '请选择权限类型' }]}
                                    initialValue={TenantPermissionType.ACTION}
                                >
                                    <Select
                                        className="w-full rounded-lg h-10 flex items-center"
                                        placeholder="选择权限类型"
                                        options={typeOptions}
                                    />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="path"
                                    label="路径"
                                >
                                    <Input
                                        className="w-full rounded-lg h-10"
                                        placeholder="输入路径（可选）"
                                        maxLength={256}
                                        showCount
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="description"
                                    label="描述"
                                >
                                    <Input.TextArea
                                        className="w-full rounded-lg"
                                        placeholder="输入描述（可选）"
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
                    return (await TenantPermissionManagerController.query(props)).data!
                }}
                tableActions={[
                    {
                        label: <span>权限类型</span>,
                        children: <Select
                            defaultValue="-1"
                            style={{ width: 120 }}
                            options={[
                                { value: '-1', label: '全部' },
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
                    return (await TenantPermissionManagerController.delete(props)).data!
                }}
                update={async (props: ManagerUpdateTenantPermissionDTO) => {
                    return (await TenantPermissionManagerController.update(props)).data!
                }}
                create={async (props) => {
                    return (await TenantPermissionManagerController.create(props as ManagerCreateTenantPermissionDTO)).data!
                }}
            />
        </>
    )
}
