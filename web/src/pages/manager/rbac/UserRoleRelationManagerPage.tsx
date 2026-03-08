import {Button, Card, message, Modal, Space, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useEffect, useState} from "react";
import {UserManagerController} from "@/api/user.api.ts";
import {getUserRoles, setUserRoles} from "@/api/user-role-relation.api.ts";
import {UserRoleManagerController} from "@/api/user-role.api.ts";
import type {User} from "@/types/user.types.ts";
import type {UserRole} from "@/types/user-role.types.ts";
import type {Key} from "react";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

interface TransferItem {
    key: string;
    title: string;
    description: string;
}

export function UserRoleRelationManagerPage() {
    const [users, setUsers] = useState<User[]>([]);
    const [allRoles, setAllRoles] = useState<UserRole[]>([]);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [selectedRoleIds, setSelectedRoleIds] = useState<Key[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    const fetchUsers = async (page = currentPage, pageSize = currentPageSize) => {
        setLoading(true);
        try {
            const res = await UserManagerController.query({ page, pageSize });
            setUsers(res.data?.records || []);
            setTotal(res.data?.total || 0);
        } catch {
            void message.error("无法获取用户列表");
        } finally {
            setLoading(false);
        }
    };

    const fetchAllRoles = async () => {
        try {
            const res = await UserRoleManagerController.list();
            setAllRoles(res.data || []);
        } catch {
            void message.error("无法获取角色列表");
        }
    };

    const handlePageChange = (page: number, pageSize: number) => {
        setCurrentPage(page);
        setCurrentPageSize(pageSize);
        void fetchUsers(page, pageSize);
    };

    const openAssignModal = async (user: User) => {
        setSelectedUser(user);
        setIsModalVisible(true);
        try {
            const res = await getUserRoles(user.id);
            const ids = res.data?.map(r => String(r.id)) || [];
            console.log("Loaded roles for user:", user.id, ids);
            setSelectedRoleIds(ids);
        } catch {
            void message.error("无法获取用户角色");
            setSelectedRoleIds([]);
        }
    };

    const handleSave = async () => {
        if (!selectedUser) return;
        const ids = selectedRoleIds.map(String);
        console.log("Saving roles for user:", selectedUser.id, ids);
        setSaving(true);
        try {
            await setUserRoles(selectedUser.id, ids);
            void message.success("角色分配成功");
            setIsModalVisible(false);
        } catch {
            void message.error("角色分配失败");
        } finally {
            setSaving(false);
        }
    };

    const handleTransferChange = (targetKeys: Key[]) => {
        console.log("Transfer changed:", targetKeys);
        setSelectedRoleIds(targetKeys);
    };

    const transferData: TransferItem[] = allRoles.map(r => ({
        key: String(r.id),
        title: r.name,
        description: r.description || ''
    }));

    useEffect(() => {
        void fetchUsers(currentPage, currentPageSize);
        void fetchAllRoles();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const columns = [
        {
            title: "用户",
            dataIndex: "nickname",
            key: "nickname",
            render: (_: unknown, row: User) => (
                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.nickname}>
                        <span className="text-xs font-mono">{row.nickname}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: "用户名",
            dataIndex: "username",
            key: "username",
            render: (_: unknown, row: User) => (
                <span className="text-xs font-mono">{row.username}</span>
            )
        },
        {
            title: "邮箱",
            dataIndex: "email",
            key: "email",
            render: (_: unknown, row: User) => (
                <span className="text-xs font-mono">{row.email}</span>
            )
        },
        {
            title: "操作",
            key: "action",
            render: (_: unknown, row: User) => (
                <Button type="primary" size="small" onClick={() => openAssignModal(row)}>
                    分配角色
                </Button>
            )
        }
    ];

    return (
        <>
            <ActionBarComponent
                title="用户角色管理"
                subtitle="为用户分配系统角色"
            />

            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <Table
                    columns={columns}
                    dataSource={users}
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
                title={`为用户 "${selectedUser?.nickname}" 分配角色`}
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
                    titles={['可用角色', '已分配角色']}
                    targetKeys={selectedRoleIds}
                    onChange={handleTransferChange}
                    render={item => `${item.title} (${item.description})`}
                    listStyle={{
                        width: 300,
                        height: 400,
                    }}
                />
            </Modal>
        </>
    );
}
