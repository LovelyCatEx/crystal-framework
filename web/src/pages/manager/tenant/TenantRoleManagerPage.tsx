import {Button, Col, Form, Input, message, Modal, Row, Tag, Transfer} from "antd";
import type {Key} from "react";
import {useEffect, useRef, useState} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantRoleDTO,
    type ManagerUpdateTenantRoleDTO,
    TenantRoleManagerController
} from "@/api/tenant-role.api.ts";
import {TENANT_ROLE_TABLE_COLUMNS} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantRoleIdSelector} from "@/components/selector/TenantRoleIdSelector.tsx";
import {PlusOutlined} from "@ant-design/icons";
import type {TenantRole} from "@/types/tenat-role.types.ts";
import {getTenantRolePermissions, setTenantRolePermissions} from "@/api/tenant-role-permission.api.ts";
import {TenantPermissionManagerController} from "@/api/tenant-permission.api.ts";
import {TenantPermissionType, type TenantPermission} from "@/types/tenant-permission.types.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
    type: TenantPermissionType;
    path?: string | null;
}

export function TenantRoleManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    // Permission assignment modal states
    const [allPermissions, setAllPermissions] = useState<TenantPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<TenantRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
    const [savingPermissions, setSavingPermissions] = useState(false);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
        pageRef?.current?.refreshData?.();
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const fetchAllPermissions = async () => {
        try {
            const res = await TenantPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error("无法获取权限列表");
        }
    };

    const openAssignPermissionModal = async (role: TenantRole) => {
        setSelectedRole(role);
        setIsPermissionModalVisible(true);
        try {
            const res = await getTenantRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error("无法获取角色权限");
            setSelectedPermissionIds([]);
        }
    };

    const handleSavePermissions = async () => {
        if (!selectedRole) return;
        const ids = selectedPermissionIds.map(String);
        setSavingPermissions(true);
        try {
            await setTenantRolePermissions(selectedRole.id, ids);
            void message.success("权限分配成功");
            setIsPermissionModalVisible(false);
        } catch {
            void message.error("权限分配失败");
        } finally {
            setSavingPermissions(false);
        }
    };

    const handleTransferChange = (targetKeys: Key[]) => {
        setSelectedPermissionIds(targetKeys);
    };

    const transferData: TransferItem[] = allPermissions.map(p => ({
        key: String(p.id),
        title: p.name,
        description: p.description || '',
        type: TenantPermissionType[p.type] as unknown as TenantPermissionType,
        path: p.path
    }));

    useEffect(() => {
        void fetchAllPermissions();
    }, []);

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
                    editModalFormChildren={(editingItem: TenantRole | null) => (
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
                                        <TenantRoleIdSelector tenantId={selectedTenantId || ''} disabledRoleId={editingItem?.id ?? null} />
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
                    tableRowActionsRender={(record: TenantRole) => (
                        <Button
                            size="small"
                            onClick={() => openAssignPermissionModal(record)}
                        >
                            分配权限
                        </Button>
                    )}
                />
            )}

            <Modal
                title={`为角色 "${selectedRole?.name}" 分配权限`}
                open={isPermissionModalVisible}
                onOk={handleSavePermissions}
                onCancel={() => setIsPermissionModalVisible(false)}
                confirmLoading={savingPermissions}
                width={1200}
                centered
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
            >
                <Transfer
                    dataSource={transferData}
                    titles={['可用权限', '已分配权限']}
                    targetKeys={selectedPermissionIds}
                    onChange={handleTransferChange}
                    render={item => {
                        return <span>
                            <Tag color="orange">{item.type}</Tag>
                            &nbsp;{item.title}
                            &nbsp;{item.path ? <Tag>{item.path}</Tag> : <span className="text-gray-500">({item.description})</span>}
                        </span>
                    }}
                    listStyle={{
                        width: 550,
                        height: 500,
                    }}
                />
            </Modal>
        </>
    )
}
