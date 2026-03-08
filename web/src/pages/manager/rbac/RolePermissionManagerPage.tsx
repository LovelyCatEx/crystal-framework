import {Button, Card, message, Modal, Space, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {UserRoleManagerController} from "@/api/user-role.api.ts";
import {getRolePermissions, setRolePermissions} from "@/api/user-role-permission.api.ts";
import {UserPermissionManagerController} from "@/api/user-permission.api.ts";
import {PermissionType, type UserPermission} from "@/types/user-permission.types.ts";
import type {UserRole} from "@/types/user-role.types.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

interface TransferItem {
    key: string;
    title: string;
    description: string;
    type: PermissionType;
    path?: string | null;
}

export function RolePermissionManagerPage() {
    const [roles, setRoles] = useState<UserRole[]>([]);
    const [allPermissions, setAllPermissions] = useState<UserPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    const fetchRoles = async (page = currentPage, pageSize = currentPageSize) => {
        setLoading(true);
        try {
            const res = await UserRoleManagerController.query({ page, pageSize });
            setRoles(res.data?.records || []);
            setTotal(res.data?.total || 0);
        } catch {
            void message.error("无法获取角色列表");
        } finally {
            setLoading(false);
        }
    };

    const fetchAllPermissions = async () => {
        try {
            const res = await UserPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch(err) {
            void message.error("无法获取权限列表");
        }
    };

    const handlePageChange = (page: number, pageSize: number) => {
        setCurrentPage(page);
        setCurrentPageSize(pageSize);
        void fetchRoles(page, pageSize);
    };

    const openAssignModal = async (role: UserRole) => {
        setSelectedRole(role);
        setIsModalVisible(true);
        try {
            const res = await getRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            console.log("Loaded permissions for role:", role.id, ids);
            setSelectedPermissionIds(ids);
        } catch {
            void message.error("无法获取角色权限");
            setSelectedPermissionIds([]);
        }
    };

    const handleSave = async () => {
        if (!selectedRole) return;
        const ids = selectedPermissionIds.map(String);
        console.log("Saving permissions for role:", selectedRole.id, ids);
        setSaving(true);
        try {
            await setRolePermissions(selectedRole.id, ids);
            void message.success("权限分配成功");
            setIsModalVisible(false);
        } catch {
            void message.error("权限分配失败");
        } finally {
            setSaving(false);
        }
    };

    const handleTransferChange = (targetKeys: Key[]) => {
        console.log("Transfer changed:", targetKeys);
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
        void fetchRoles(currentPage, currentPageSize);
        void fetchAllPermissions();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const columns = [
        {
            title: "角色",
            dataIndex: "name",
            key: "name",
            render: (_: unknown, row: UserRole) => (
                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.name}>
                        <span className="text-xs font-mono">{row.name}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: "描述",
            dataIndex: "description",
            key: "description",
            render: (_: unknown, row: UserRole) => (
                <span className="text-xs font-mono">{row.description}</span>
            )
        },
        {
            title: "操作",
            key: "action",
            render: (_: unknown, row: UserRole) => (
                <Button type="primary" size="small" onClick={() => openAssignModal(row)}>
                    分配权限
                </Button>
            )
        }
    ];

    return (
        <>
            <ActionBarComponent
                title="角色权限管理"
                subtitle="为角色分配系统权限"
            />

            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <Table
                    columns={columns}
                    dataSource={roles}
                    rowKey="id"
                    loading={loading}
                    pagination={{
                        showSizeChanger: true,
                        defaultPageSize: 20,
                        className: "pr-6",
                        current: currentPage,
                        total: total,
                        pageSize: currentPageSize,
                        pageSizeOptions: [5, 10, 15, 20],
                        onChange: handlePageChange
                    }}
                />
            </Card>

            <Modal
                title={`为角色 "${selectedRole?.name}" 分配权限`}
                open={isModalVisible}
                onCancel={() => setIsModalVisible(false)}
                onOk={handleSave}
                width={700}
                centered
                confirmLoading={saving}
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
            >
                <Transfer
                    dataSource={transferData}
                    titles={['可用权限', '已分配权限']}
                    targetKeys={selectedPermissionIds}
                    onChange={handleTransferChange}
                    render={item => {
                        return <CopyableToolTip title={`${item.path ? item.path : item.description}`}>
                            <span><Tag color="orange">{item.type}</Tag> {item.title}</span>
                        </CopyableToolTip>
                    }}
                    listStyle={{
                        width: 300,
                        height: 400,
                    }}
                />
            </Modal>
        </>
    );
}
