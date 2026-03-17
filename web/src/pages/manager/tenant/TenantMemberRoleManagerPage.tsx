import {Button, Card, message, Modal, Space, Table, Tag, Transfer} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {getTenantMemberRoles, setTenantMemberRoles} from "@/api/tenant-member-role.api.ts";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";
import type {TenantRole} from "@/types/tenat-role.types.ts";
import {useTranslation} from "react-i18next";

interface TransferItem {
    key: string;
    title: string;
    description: string;
}

export function TenantMemberRoleManagerPage() {
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
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
            void message.error(t('pages.tenantMemberRoleManager.messages.fetchMembersFailed'));
        } finally {
            setLoading(false);
        }
    };

    const fetchAllRoles = async () => {
        try {
            if (!selectedTenantId) return;
            const res = await TenantRoleManagerController.list({ tenantId: selectedTenantId });
            setAllRoles(res.data || []);
        } catch {
            void message.error(t('pages.tenantMemberRoleManager.messages.fetchRolesFailed'));
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
            void message.error(t('pages.tenantMemberRoleManager.messages.fetchMemberRolesFailed'));
            setSelectedRoleIds([]);
        }
    };

    const handleSave = async () => {
        if (!selectedMember) return;
        const ids = selectedRoleIds.map(String);
        setSaving(true);
        try {
            await setTenantMemberRoles(selectedMember.id, ids);
            void message.success(t('pages.tenantMemberRoleManager.messages.assignSuccess'));
            setIsModalVisible(false);
        } catch {
            void message.error(t('pages.tenantMemberRoleManager.messages.assignFailed'));
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
            title: t('pages.tenantMemberRoleManager.columns.member'),
            dataIndex: "user",
            key: "user",
            render: (_: unknown, row: TenantMemberVO) => (
                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.user?.nickname || t('common.unknownUser')}>
                        <span className="text-xs font-mono">{row.user?.nickname || t('common.unknownUser')}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: t('pages.tenantMemberRoleManager.columns.username'),
            key: "username",
            render: (_: unknown, row: TenantMemberVO) => (
                <span className="text-xs font-mono">{row.user?.username || '-'}</span>
            )
        },
        {
            title: t('pages.tenantMemberRoleManager.columns.email'),
            key: "email",
            render: (_: unknown, row: TenantMemberVO) => (
                <span className="text-xs font-mono">{row.user?.email || '-'}</span>
            )
        },
        {
            title: t('pages.tenantMemberRoleManager.columns.status'),
            dataIndex: "status",
            key: "status",
            render: (status: number) => {
                const statusColors: Record<number, string> = {
                    0: 'default',
                    1: 'red',
                    2: 'orange',
                    3: 'blue',
                    4: 'green'
                };
                return (
                    <Tag color={statusColors[status] || 'default'} className="text-xs">
                        {getTenantMemberStatus(status)}
                    </Tag>
                );
            }
        },
        {
            title: t('pages.tenantMemberRoleManager.columns.action'),
            key: "action",
            render: (_: unknown, row: TenantMemberVO) => (
                <Button type="primary" size="small" onClick={() => openAssignModal(row)}>
                    {t('pages.tenantMemberRoleManager.action.assignRole')}
                </Button>
            )
        }
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantMemberRoleManager.title')}
                subtitle={t('pages.tenantMemberRoleManager.subtitle')}
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
                            onChange: handlePageChange
                        }}
                    />
                </Card>
            )}

            <Modal
                title={t('pages.tenantMemberRoleManager.modal.title', { nickname: selectedMember?.user?.nickname || '' })}
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
                    titles={[t('pages.tenantMemberRoleManager.modal.titles.unassigned'), t('pages.tenantMemberRoleManager.modal.titles.assigned')]}
                    listStyle={{
                        width: 300,
                        height: 400
                    }}
                />
            </Modal>
        </>
    );
}
