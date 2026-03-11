import {Button, Card, message, Modal, Space, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {getTenantMemberRoles, setTenantMemberRoles} from "@/api/tenant-member-role.api.ts";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";
import type {TenantRole} from "@/types/tenat-role.types.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
}

export function TenantMemberRoleManagerPage() {
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [members, setMembers] = useState<TenantMemberVO[]>([]);
    const [allRoles, setAllRoles] = useState<TenantRole[]>([]);
    const [selectedMember, setSelectedMember] = useState<TenantMemberVO | null>(null);
    const [selectedRoleIds, setSelectedRoleIds] = useState<Key[]>([]);
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

    const fetchMembers = async (page = currentPage, pageSize = currentPageSize, tenantId = selectedTenantId) => {
        if (!tenantId) return;
        setLoading(true);
        try {
            const res = await TenantMemberManagerController.query({
                page,
                pageSize,
                tenantId
            });
            setMembers(res.data?.records || []);
            setTotal(res.data?.total || 0);
        } catch {
            void message.error("无法获取成员列表");
        } finally {
            setLoading(false);
        }
    };

    const fetchAllRoles = async () => {
        try {
            const res = await TenantRoleManagerController.list();
            setAllRoles(res.data || []);
        } catch {
            void message.error("无法获取角色列表");
        }
    };

    const handlePageChange = (page: number, pageSize: number) => {
        setCurrentPage(page);
        setCurrentPageSize(pageSize);
        void fetchMembers(page, pageSize);
    };

    const openAssignModal = async (member: TenantMemberVO) => {
        setSelectedMember(member);
        setIsModalVisible(true);
        try {
            const res = await getTenantMemberRoles(member.id);
            const ids = res.data?.map(r => String(r.id)) || [];
            setSelectedRoleIds(ids);
        } catch {
            void message.error("无法获取成员角色");
            setSelectedRoleIds([]);
        }
    };

    const handleSave = async () => {
        if (!selectedMember) return;
        const ids = selectedRoleIds.map(String);
        setSaving(true);
        try {
            await setTenantMemberRoles(selectedMember.id, ids);
            void message.success("角色分配成功");
            setIsModalVisible(false);
        } catch {
            void message.error("角色分配失败");
        } finally {
            setSaving(false);
        }
    };

    const handleTransferChange = (targetKeys: Key[]) => {
        setSelectedRoleIds(targetKeys);
    };

    const transferData: TransferItem[] = allRoles.map(r => ({
        key: String(r.id),
        title: r.name,
        description: r.description || ''
    }));

    useEffect(() => {
        if (selectedTenantId) {
            void fetchMembers(1, currentPageSize, selectedTenantId);
            void fetchAllRoles();
        } else {
            setMembers([]);
            setAllRoles([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedTenantId]);

    const columns = [
        {
            title: "成员",
            dataIndex: "user",
            key: "user",
            render: (_: unknown, row: TenantMemberVO) => (
                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.user?.nickname || '未知用户'}>
                        <span className="text-xs font-mono">{row.user?.nickname || '未知用户'}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: "用户名",
            key: "username",
            render: (_: unknown, row: TenantMemberVO) => (
                <span className="text-xs font-mono">{row.user?.username || '-'}</span>
            )
        },
        {
            title: "邮箱",
            key: "email",
            render: (_: unknown, row: TenantMemberVO) => (
                <span className="text-xs font-mono">{row.user?.email || '-'}</span>
            )
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            render: (status: number) => (
                <Tag color={status === 1 ? 'green' : 'red'} className="text-xs">
                    {status === 1 ? 'ACTIVE' : 'INACTIVE'}
                </Tag>
            )
        },
        {
            title: "操作",
            key: "action",
            render: (_: unknown, row: TenantMemberVO) => (
                <Button type="primary" size="small" onClick={() => openAssignModal(row)}>
                    分配角色
                </Button>
            )
        }
    ];

    return (
        <>
            <ActionBarComponent
                title="租户成员角色管理"
                subtitle="为租户成员分配角色"
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
                <Card className="mt-4 border-none shadow-sm rounded-2xl overflow-hidden">
                    <Table
                        columns={columns}
                        dataSource={members}
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
                title={`为成员 "${selectedMember?.user?.nickname}" 分配角色`}
                open={isModalVisible}
                onOk={handleSave}
                onCancel={() => setIsModalVisible(false)}
                confirmLoading={saving}
                width={700}
            >
                <Transfer
                    dataSource={transferData}
                    targetKeys={selectedRoleIds}
                    onChange={handleTransferChange}
                    render={item => item.title}
                    titles={['未分配角色', '已分配角色']}
                    listStyle={{
                        width: 300,
                        height: 400
                    }}
                />
            </Modal>
        </>
    );
}
