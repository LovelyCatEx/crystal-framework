import {Button, Card, message, Modal, Space, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {TenantRoleManagerController, type TenantRoleVO} from "@/api/tenant-role.api.ts";
import {getTenantRolePermissions, setTenantRolePermissions} from "@/api/tenant-role-permission.api.ts";
import {TenantPermissionManagerController, type TenantPermissionVO} from "@/api/tenant-permission.api.ts";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

interface TransferItem {
    key: string;
    title: string;
    description: string;
}

export function TenantRolePermissionManagerPage() {
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [roles, setRoles] = useState<TenantRoleVO[]>([]);
    const [allPermissions, setAllPermissions] = useState<TenantPermissionVO[]>([]);
    const [selectedRole, setSelectedRole] = useState<TenantRoleVO | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
        // Reset pagination when tenant changes
        setCurrentPage(1);
    };

    const fetchRoles = async (page = currentPage, pageSize = currentPageSize, tenantId = selectedTenantId) => {
        if (!tenantId) return;
        setLoading(true);
        try {
            const res = await TenantRoleManagerController.query({
                page,
                pageSize,
                tenantId
            });
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
            const res = await TenantPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error("无法获取权限列表");
        }
    };

    const handlePageChange = (page: number, pageSize: number) => {
        setCurrentPage(page);
        setCurrentPageSize(pageSize);
        void fetchRoles(page, pageSize);
    };

    const openAssignModal = async (role: TenantRoleVO) => {
        setSelectedRole(role);
        setIsModalVisible(true);
        try {
            const res = await getTenantRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error("无法获取角色权限");
            setSelectedPermissionIds([]);
        }
    };

    const handleSave = async () => {
        if (!selectedRole) return;
        const ids = selectedPermissionIds.map(String);
        setSaving(true);
        try {
            await setTenantRolePermissions(selectedRole.id, ids);
            void message.success("权限分配成功");
            setIsModalVisible(false);
        } catch {
            void message.error("权限分配失败");
        } finally {
            setSaving(false);
        }
    };

    const handleTransferChange = (targetKeys: Key[]) => {
        setSelectedPermissionIds(targetKeys);
    };

    const transferData: TransferItem[] = allPermissions.map(p => ({
        key: String(p.id),
        title: p.name,
        description: p.description || ''
    }));

    useEffect(() => {
        if (selectedTenantId) {
            void fetchRoles(1, currentPageSize, selectedTenantId);
            void fetchAllPermissions();
        } else {
            setRoles([]);
            setAllPermissions([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedTenantId]);

    const columns = [
        {
            title: "角色",
            dataIndex: "name",
            key: "name",
            render: (_: unknown, row: TenantRoleVO) => (
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
            render: (_: unknown, row: TenantRoleVO) => (
                <span className="text-xs font-mono">{row.description || '-'}</span>
            )
        },
        {
            title: "操作",
            key: "action",
            render: (_: unknown, row: TenantRoleVO) => (
                <Button type="primary" size="small" onClick={() => openAssignModal(row)}>
                    分配权限
                </Button>
            )
        }
    ];

    return (
        <>
            <ActionBarComponent
                title="租户角色权限管理"
                subtitle="为租户角色分配权限"
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
                <Card className="mt-4 border-none shadow-sm rounded-2xl overflow-hidden">
                    <Table
                        columns={columns}
                        dataSource={roles}
                        rowKey="id"
                        loading={loading}
                        pagination={{
                            current: currentPage,
                            pageSize: currentPageSize,
                            total: total,
                            showSizeChanger: true,
                            showQuickJumper: true,
                            showTotal: (total) => `共 ${total} 条`,
                            onChange: handlePageChange
                        }}
                    />
                </Card>
            )}

            <Modal
                title={`为角色 "${selectedRole?.name}" 分配权限`}
                open={isModalVisible}
                onOk={handleSave}
                onCancel={() => setIsModalVisible(false)}
                confirmLoading={saving}
                width={700}
            >
                <Transfer
                    dataSource={transferData}
                    targetKeys={selectedPermissionIds}
                    onChange={handleTransferChange}
                    render={item => item.title}
                    titles={['未分配权限', '已分配权限']}
                    listStyle={{
                        width: 300,
                        height: 400
                    }}
                />
            </Modal>
        </>
    );
}
