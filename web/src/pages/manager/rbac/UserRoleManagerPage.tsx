import {Button, Col, Form, Input, message, Modal, Row, Tag, Transfer} from "antd";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {ManagerPageContainer} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateRoleDTO, UserRoleManagerController} from "@/api/user-role.api.ts";
import TextArea from "antd/es/input/TextArea";
import {USER_ROLE_MANAGER_TABLE_COLUMNS} from "@/components/columns/UserRoleEntityColumns.tsx";
import {getRolePermissions, setRolePermissions} from "@/api/user-role-permission.api.ts";
import {UserPermissionManagerController} from "@/api/user-permission.api.ts";
import {PermissionType, type UserPermission} from "@/types/user-permission.types.ts";
import type {UserRole} from "@/types/user-role.types.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
    type: PermissionType;
    path?: string | null;
}

export function UserRoleManagerPage() {
    const [allPermissions, setAllPermissions] = useState<UserPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
    const [savingPermissions, setSavingPermissions] = useState(false);

    const fetchAllPermissions = async () => {
        try {
            const res = await UserPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error("无法获取权限列表");
        }
    };

    const openAssignPermissionModal = async (role: UserRole) => {
        setSelectedRole(role);
        setIsPermissionModalVisible(true);
        try {
            const res = await getRolePermissions(role.id);
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
            await setRolePermissions(selectedRole.id, ids);
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
        type: PermissionType[p.type] as unknown as PermissionType,
        path: p.path
    }));

    useEffect(() => {
        void fetchAllPermissions();
    }, []);

    return (
        <>
            <ManagerPageContainer
                entityName="用户角色"
                title="用户角色管理"
                subtitle="配置系统用户角色列表"
                columns={USER_ROLE_MANAGER_TABLE_COLUMNS}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item name="name" label="角色名称" rules={[{ required: true }, { max: 128, message: '角色名称长度不能超过128个字符' }]}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={128} showCount />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Form.Item name="description" label="角色描述" rules={[{ max: 512, message: '角色描述长度不能超过512个字符' }]}>
                            <TextArea rows={2} placeholder="输入角色描述..." className="rounded-lg" maxLength={512} showCount />
                        </Form.Item>
                    </>
                }
                query={async (props) => {
                    return (await UserRoleManagerController.query(props)).data!
                }}
                delete={async (props) => {
                    return (await UserRoleManagerController.delete(props)).data!
                }}
                update={async (props) => {
                    return (await UserRoleManagerController.update(props)).data!
                }}
                create={async (props) => {
                    return (await UserRoleManagerController.create(props as ManagerCreateRoleDTO)).data!
                }}
                tableRowActionsRender={(record: UserRole) => (
                    <Button
                        size="small"
                        onClick={() => openAssignPermissionModal(record)}
                    >
                        分配权限
                    </Button>
                )}
            />

            <Modal
                title={`为角色 "${selectedRole?.name}" 分配权限`}
                open={isPermissionModalVisible}
                onCancel={() => setIsPermissionModalVisible(false)}
                onOk={handleSavePermissions}
                width={1200}
                centered
                confirmLoading={savingPermissions}
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
