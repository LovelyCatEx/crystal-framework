import {Button, Card, message, Modal, Space, Spin, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {TenantMemberStatusMap} from "@/types/tenant-member.types.ts";
import {tenantMemberStatusToTranslationMap} from "@/i18n/tenant-member.ts";
import {getTenantMemberRoles, setTenantMemberRoles} from "@/api/tenant-member-role.api.ts";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";
import type {TenantRole} from "@/types/tenat-role.types.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
}

export function MyTenantMemberRoleManagerPage() {
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const currentTenantId = currentTenant?.tenantId ?? null;
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

    const fetchMembers = async (page = currentPage, pageSize = currentPageSize, tenantId = currentTenantId) => {
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
            if (!currentTenantId) return;
            const res = await TenantRoleManagerController.list({ tenantId: currentTenantId });
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
        if (currentTenantId) {
            void fetchMembers(1, currentPageSize, currentTenantId);
            void fetchAllRoles();
        } else {
            setMembers([]);
            setAllRoles([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentTenantId]);

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
            render: (status: number) => {
                const statusInfo = TenantMemberStatusMap[status] || { label: '未知', color: 'default' };
                const translatedLabel = tenantMemberStatusToTranslationMap.get(status) || statusInfo.label;
                return (
                    <Tag color={statusInfo.color} className="text-xs">
                        {translatedLabel}
                    </Tag>
                );
            }
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

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title="我的成员角色管理" subtitle="为当前组织成员分配角色" />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title="我的成员角色管理"
                subtitle="为当前组织成员分配角色"
            />
            {currentTenantId && (
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
