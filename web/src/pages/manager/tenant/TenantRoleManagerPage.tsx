import {Button, Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantRoleDTO,
    type ManagerUpdateTenantRoleDTO,
    type TenantRoleVO,
    TenantRoleManagerController
} from "@/api/tenant-role.api.ts";
import {useRef, useState} from "react";
import {TENANT_ROLE_TABLE_COLUMNS} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantRoleIdSelector} from "@/components/selector/TenantRoleIdSelector.tsx";
import {PlusOutlined} from "@ant-design/icons";

export function TenantRoleManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title="租户角色管理"
                subtitle="管理租户角色信息"
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            新增租户角色
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
                    entityName="租户角色"
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={TENANT_ROLE_TABLE_COLUMNS}
                    editModalFormChildren={(editingItem: TenantRoleVO | null) => (
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={selectedTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="name"
                                        label="角色名称"
                                        rules={[{ required: true, message: '请输入角色名称' }]}
                                    >
                                        <Input
                                            className="w-full rounded-lg h-10"
                                            placeholder="输入角色名称"
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="parentId"
                                        label="父角色"
                                    >
                                        <TenantRoleIdSelector disabledRoleId={editingItem?.id ?? null} />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={24}>
                                    <Form.Item
                                        name="description"
                                        label="描述"
                                    >
                                        <Input.TextArea
                                            className="w-full rounded-lg"
                                            placeholder="输入描述（可选）"
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
                            tenantId: selectedTenantId
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
                            tenantId: selectedTenantId
                        };
                        return (await TenantRoleManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
