import {Button, Card, Col, Form, Input, message, Modal, Popover, Row, Space, Table, Tag, theme} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantDepartmentIdSelector} from "@/components/selector/TenantDepartmentIdSelector.tsx";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {TenantDepartmentPopCard} from "@/components/card/pop/TenantDepartmentPopCard.tsx";
import {TreeDetailLayout} from "@/components/layouts/TreeDetailLayout.tsx";
import {useEntityTree} from "@/compositions/use-entity-tree.ts";
import {
    type ManagerCreateTenantDepartmentDTO,
    type ManagerUpdateTenantDepartmentDTO,
    TenantDepartmentManagerController
} from "@/api/tenant/tenant-department.api.ts";
import {
    DepartmentMemberRoleType,
    type ManagerReadTenantDepartmentMemberDTO,
    TenantDepartmentMemberManagerController
} from "@/api/tenant/tenant-department-member.api.ts";
import {getDepartmentMemberRoleType} from "@/i18n/enum-helpers.ts";
import {TenantMemberManagerController} from "@/api/tenant/tenant-member.api.ts";
import {useTenantMemberTableColumns} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {useTenantDepartmentMemberTableColumns} from "@/components/columns/TenantDepartmentMemberEntityColumns.tsx";

import {
    DeleteOutlined,
    EditOutlined,
    ExclamationCircleFilled,
    PlusOutlined,
    SettingOutlined,
    TeamOutlined,
    UserAddOutlined
} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import type {TenantDepartment} from "@/types/tenant/tenant-department.types.ts";
import type {TenantDepartmentMemberVO} from "@/types/tenant/tenant-department-member.types.ts";
import type {TenantMemberVO} from "@/types/tenant/tenant-member.types.ts";
import {useTranslation} from "react-i18next";

export default function TenantDepartmentManagerPage() {
    const {token} = theme.useToken();
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
    const baseDepartmentMemberColumns = useTenantDepartmentMemberTableColumns();
    const memberColumnsForSelector = useTenantMemberTableColumns();

    // Tree state (departments + selection + URL sync) is fully owned by useEntityTree.
    const {
        items: departments,
        treeData,
        loading,
        selectedItem: selectedDepartment,
        selectItem: selectDepartment,
        refresh: refreshDepartments,
        clearSelection: clearDepartmentSelection
    } = useEntityTree<TenantDepartment>({
        fetch: async () => {
            if (!selectedTenantId) return [];
            const res = await TenantDepartmentManagerController.list({tenantId: selectedTenantId});
            return res.data ?? [];
        },
        renderNodeTitle: (dept) => (
            <Space size="small">
                <span>{dept.name}</span>
            </Space>
        ),
        urlParamKey: 'departmentId',
        deps: [selectedTenantId],
        onFetchError: () => void message.error(t('pages.tenantDepartmentManager.messages.fetchDepartmentsFailed'))
    });

    const [memberLoading, setMemberLoading] = useState(false);
    const [departmentMembers, setDepartmentMembers] = useState<TenantDepartmentMemberVO[]>([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    // Modal states
    const [isEditModalVisible, setIsEditModalVisible] = useState(false);
    const [isMemberSelectorVisible, setIsMemberSelectorVisible] = useState(false);
    const [isRoleEditModalVisible, setIsRoleEditModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<TenantDepartment | null>(null);
    const [editingMember, setEditingMember] = useState<TenantDepartmentMemberVO | null>(null);
    const [editingRoleType, setEditingRoleType] = useState<number>(DepartmentMemberRoleType.MEMBER);
    const [saving, setSaving] = useState(false);
    const [updatingRole, setUpdatingRole] = useState(false);
    const [form] = Form.useForm();

    // Current department member IDs (for disabling already added members)
    const [existingMemberIds, setExistingMemberIds] = useState<Set<string>>(new Set());
    const [modal, contextHolder] = Modal.useModal();

    const fetchDepartmentMembers = async (departmentId: string, page = currentPage, pageSize = currentPageSize) => {
        setMemberLoading(true);
        try {
            const queryDTO: ManagerReadTenantDepartmentMemberDTO = {
                departmentId,
                page,
                pageSize
            };
            const res = await TenantDepartmentMemberManagerController.query(queryDTO);
            const data = res.data;
            if (data) {
                setDepartmentMembers(data.records);
                setTotal(data.total);
                // Store existing member IDs for disabling in selector
                setExistingMemberIds(new Set(data.records.map((m: TenantDepartmentMemberVO) => String(m.member.id))));
            }
        } catch {
            void message.error(t('pages.tenantDepartmentManager.messages.fetchMembersFailed'));
        } finally {
            setMemberLoading(false);
        }
    };

    // Reload members whenever the tree selection changes (user click, URL restore,
    // or tenant switch clearing to null).
    useEffect(() => {
        if (selectedDepartment) {
            void fetchDepartmentMembers(selectedDepartment.id);
        } else {
            setDepartmentMembers([]);
            setTotal(0);
            setExistingMemberIds(new Set());
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedDepartment?.id]);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

    const openAddModal = () => {
        setEditingItem(null);
        form.resetFields();
        form.setFieldsValue({tenantId: selectedTenantId});
        setIsEditModalVisible(true);
    };

    const openEditModal = (dept: TenantDepartment) => {
        setEditingItem(dept);
        form.setFieldsValue({
            id: dept.id,
            tenantId: dept.tenantId,
            name: dept.name,
            description: dept.description,
            parentId: dept.parentId
        });
        setIsEditModalVisible(true);
    };

    const handleDelete = (dept: TenantDepartment) => {
        modal.confirm({
            title: t('pages.tenantDepartmentManager.modal.delete.title'),
            icon: <ExclamationCircleFilled/>,
            content: t('pages.tenantDepartmentManager.modal.delete.content', {name: dept.name}),
            onOk() {
                return TenantDepartmentManagerController.delete({ids: [dept.id]})
                    .then(() => {
                        void message.success(t('pages.tenantDepartmentManager.messages.deleteSuccess'));
                        if (selectedDepartment?.id === dept.id) {
                            clearDepartmentSelection();
                        }
                        void refreshDepartments();
                    })
                    .catch(() => {
                        void message.error(t('pages.tenantDepartmentManager.messages.deleteFailed'));
                    });
            },
        });
    };

    const handleSave = async () => {
        try {
            const values = await form.validateFields();
            setSaving(true);

            if (editingItem) {
                await TenantDepartmentManagerController.update(values as ManagerUpdateTenantDepartmentDTO);
                void message.success(t('pages.tenantDepartmentManager.messages.updateSuccess'));
            } else {
                await TenantDepartmentManagerController.create(values as ManagerCreateTenantDepartmentDTO);
                void message.success(t('pages.tenantDepartmentManager.messages.createSuccess'));
            }

            setIsEditModalVisible(false);
            void refreshDepartments();
        } catch {
            // Form validation error or API error
        } finally {
            setSaving(false);
        }
    };

    const openMemberSelector = () => {
        if (!selectedDepartment) return;
        setIsMemberSelectorVisible(true);
    };

    const handleMemberSelectorCancel = () => {
        setIsMemberSelectorVisible(false);
    };

    const handleMemberSelectorOk = async (selectedMembers: TenantMemberVO[]) => {
        if (!selectedDepartment || selectedMembers.length === 0) {
            setIsMemberSelectorVisible(false);
            return;
        }

        try {
            // Create new member relations
            for (const member of selectedMembers) {
                await TenantDepartmentMemberManagerController.create({
                    departmentId: selectedDepartment.id,
                    memberId: member.id,
                    roleType: DepartmentMemberRoleType.MEMBER
                });
            }
            void message.success(t('pages.tenantDepartmentManager.messages.addMembersSuccess', {count: selectedMembers.length}));
            setIsMemberSelectorVisible(false);
            void fetchDepartmentMembers(selectedDepartment.id);
        } catch {
            void message.error(t('pages.tenantDepartmentManager.messages.addMembersFailed'));
        }
    };

    // Check if a member is already in the department
    const isMemberDisabled = (member: TenantMemberVO) => {
        return existingMemberIds.has(String(member.id));
    };

    const handleRemoveMember = (row: TenantDepartmentMemberVO) => {
        if (!selectedDepartment) return;
        modal.confirm({
            title: t('pages.tenantDepartmentManager.modal.removeMember.title'),
            icon: <ExclamationCircleFilled/>,
            content: t('pages.tenantDepartmentManager.modal.removeMember.content', {name: row.member.user?.nickname || row.member.user?.username || row.id}),
            onOk() {
                return TenantDepartmentMemberManagerController.delete({ids: [row.id]})
                    .then(() => {
                        void message.success(t('pages.tenantDepartmentManager.messages.removeMemberSuccess'));
                        void fetchDepartmentMembers(selectedDepartment.id);
                    })
                    .catch(() => {
                        void message.error(t('pages.tenantDepartmentManager.messages.removeMemberFailed'));
                    });
            },
        });
    };

    const openRoleEditModal = (row: TenantDepartmentMemberVO) => {
        setEditingMember(row);
        setEditingRoleType(row.roleType);
        setIsRoleEditModalVisible(true);
    };

    const handleRoleEditCancel = () => {
        setIsRoleEditModalVisible(false);
        setEditingMember(null);
    };

    const handleRoleEditSave = async () => {
        if (!selectedDepartment || !editingMember) return;
        setUpdatingRole(true);
        try {
            await TenantDepartmentMemberManagerController.update({
                id: editingMember.id,
                departmentId: selectedDepartment.id,
                memberId: editingMember.member.id,
                roleType: editingRoleType
            });
            void message.success(t('pages.tenantDepartmentManager.messages.updateRoleSuccess'));
            setIsRoleEditModalVisible(false);
            void fetchDepartmentMembers(selectedDepartment.id);
        } catch {
            void message.error(t('pages.tenantDepartmentManager.messages.updateRoleFailed'));
        } finally {
            setUpdatingRole(false);
        }
    };

    const memberColumns = [
        ...baseDepartmentMemberColumns,
        {
            title: t('pages.tenantDepartmentManager.columns.action'),
            key: "action",
            width: 150,
            render: (_: unknown, row: TenantDepartmentMemberVO) => (
                <Space size="small">
                    <Button
                        size="small"
                        icon={<SettingOutlined/>}
                        onClick={() => openRoleEditModal(row)}
                    >
                        {t('pages.tenantDepartmentManager.action.editRole')}
                    </Button>
                    <Button
                        danger
                        size="small"
                        onClick={() => handleRemoveMember(row)}
                    >
                        {t('pages.tenantDepartmentManager.action.remove')}
                    </Button>
                </Space>
            )
        }
    ];

    const treeEmptyContent = (
        <div className="flex flex-col items-center justify-center py-8 text-gray-400">
            <TeamOutlined className="text-4xl mb-4"/>
            <p className="text-sm mb-4">{t('pages.tenantDepartmentManager.empty.noDepartments')}</p>
            <Button
                type="primary"
                icon={<PlusOutlined/>}
                onClick={openAddModal}
            >
                {t('pages.tenantDepartmentManager.action.addDepartment')}
            </Button>
        </div>
    );

    const detailEmptyContent = (
        <div className="text-gray-400 text-center">
            <TeamOutlined style={{fontSize: 48}}/>
            <p className="mt-4">{t('pages.tenantDepartmentManager.empty.selectDepartment')}</p>
        </div>
    );

    const detailContent = selectedDepartment && (
        <>
            {/* Department Info Card */}
            <Card
                className="border-none shadow-sm rounded-2xl overflow-hidden mb-4"
                title={
                    <Space>
                        <span>{selectedDepartment.name}</span>
                        <Tag color="blue">ID: {selectedDepartment.id}</Tag>
                    </Space>
                }
                extra={
                    <Space>
                        <Button
                            icon={<EditOutlined/>}
                            onClick={() => openEditModal(selectedDepartment)}
                        >
                            {t('pages.tenantDepartmentManager.action.edit')}
                        </Button>
                        <Button
                            danger
                            icon={<DeleteOutlined/>}
                            onClick={() => handleDelete(selectedDepartment)}
                        >
                            {t('pages.tenantDepartmentManager.action.delete')}
                        </Button>
                    </Space>
                }
            >
                <p><strong>{t('pages.tenantDepartmentManager.info.description')}：</strong>{selectedDepartment.description || '-'}</p>
                <p>
                    <strong>{t('pages.tenantDepartmentManager.info.parentDepartment')}：</strong>
                    {selectedDepartment.parentId ? (
                        <Popover
                            content={<TenantDepartmentPopCard departmentId={selectedDepartment.parentId}/>}
                            placement="right"
                            trigger="hover"
                        >
                            <Tag color="orange" className="cursor-pointer">
                                {departments.find(d => d.id === selectedDepartment.parentId)?.name || selectedDepartment.parentId}
                            </Tag>
                        </Popover>
                    ) : (
                        '-'
                    )}
                </p>
                <p><strong>{t('pages.tenantDepartmentManager.info.createdTime')}：</strong>{formatTimestamp(selectedDepartment.createdTime)}</p>
            </Card>

            {/* Members Table */}
            <Card
                className="border-none shadow-sm rounded-2xl overflow-hidden"
                title={t('pages.tenantDepartmentManager.card.members')}
                extra={
                    <Button
                        type="primary"
                        icon={<UserAddOutlined/>}
                        onClick={openMemberSelector}
                    >
                        {t('pages.tenantDepartmentManager.action.addMember')}
                    </Button>
                }
            >
                <Table
                    columns={memberColumns}
                    dataSource={departmentMembers}
                    rowKey="id"
                    loading={memberLoading}
                    scroll={{x: 1200}}
                    pagination={{
                        current: currentPage,
                        pageSize: currentPageSize,
                        total: total,
                        showSizeChanger: true,
                        showQuickJumper: true,
                        onChange: (page, pageSize) => {
                            setCurrentPage(page);
                            setCurrentPageSize(pageSize);
                            if (selectedDepartment) {
                                void fetchDepartmentMembers(selectedDepartment.id, page, pageSize);
                            }
                        }
                    }}
                />
            </Card>
        </>
    );

    return (
        <>
            {contextHolder}
            <ActionBarComponent
                title={t('pages.tenantDepartmentManager.title')}
                subtitle={t('pages.tenantDepartmentManager.subtitle')}
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={openAddModal}
                        >
                            {t('pages.tenantDepartmentManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
                <TreeDetailLayout
                    tree={{
                        title: t('pages.tenantDepartmentManager.card.departmentList'),
                        treeData,
                        selectedKey: selectedDepartment?.id ?? null,
                        loading,
                        onSelect: selectDepartment,
                        emptyContent: treeEmptyContent
                    }}
                    detail={{
                        content: detailContent,
                        emptyContent: detailEmptyContent
                    }}
                />
            )}

            {/* Edit Modal */}
            <Modal
                title={editingItem ? t('pages.tenantDepartmentManager.modal.edit.title') : t('pages.tenantDepartmentManager.modal.add.title')}
                open={isEditModalVisible}
                onOk={handleSave}
                onCancel={() => setIsEditModalVisible(false)}
                confirmLoading={saving}
                width={600}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="id" hidden>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="tenantId" hidden>
                        <Input/>
                    </Form.Item>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="name"
                                label={t('pages.tenantDepartmentManager.modal.name.label')}
                                rules={[{required: true, message: t('pages.tenantDepartmentManager.modal.name.required')}]}
                            >
                                <Input placeholder={t('pages.tenantDepartmentManager.modal.name.placeholder')} maxLength={64} showCount/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="parentId" label={t('pages.tenantDepartmentManager.modal.parentId.label')}>
                                <TenantDepartmentIdSelector tenantId={selectedTenantId || ''} disabledDepartmentId={editingItem?.id ?? null}/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.tenantDepartmentManager.modal.description.label')}>
                        <Input.TextArea
                            placeholder={t('pages.tenantDepartmentManager.modal.description.placeholder')}
                            maxLength={512}
                            showCount
                            rows={3}
                        />
                    </Form.Item>
                </Form>
            </Modal>

            {/* Member Selector Modal */}
            <EntitySelectorModal<TenantMemberVO>
                type="checkbox"
                visible={isMemberSelectorVisible}
                title={t('pages.tenantDepartmentManager.memberSelectorModal.title', {name: selectedDepartment?.name || ''})}
                entityName={t('entityNames.tenantMember')}
                columns={memberColumnsForSelector}
                query={async (props) => {
                    return (await TenantMemberManagerController.query({
                        ...props,
                        tenantId: selectedTenantId!
                    })).data!;
                }}
                isRowDisabled={isMemberDisabled}
                onCancel={handleMemberSelectorCancel}
                onOk={handleMemberSelectorOk}
            />

            {/* Role Edit Modal */}
            <Modal
                title={t('pages.tenantDepartmentManager.roleEditModal.title', {name: editingMember?.member.user?.nickname || editingMember?.member.user?.username || ''})}
                open={isRoleEditModalVisible}
                onOk={handleRoleEditSave}
                onCancel={handleRoleEditCancel}
                confirmLoading={updatingRole}
                width={400}
            >
                <div className="py-4">
                    <p className="mb-4">{t('pages.tenantDepartmentManager.roleEditModal.description')}</p>
                    <div className="space-y-2">
                        {(() => {
                            const roleColors: Record<number, string> = {
                                0: 'default',
                                1: 'blue',
                                2: 'red'
                            };
                            return Object.entries(DepartmentMemberRoleType).map(([key, value]) => (
                                <div
                                    key={key}
                                    className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                                        editingRoleType === value
                                            ? ''
                                            : 'border-gray-200 hover:border-gray-300'
                                    }`}
                                    style={editingRoleType === value ? {
                                        borderColor: token.colorPrimary,
                                        backgroundColor: token.colorPrimaryBg
                                    } : {}}
                                    onClick={() => setEditingRoleType(value)}
                                >
                                    <div className="flex items-center gap-3">
                                        <input
                                            type="radio"
                                            checked={editingRoleType === value}
                                            onChange={() => setEditingRoleType(value)}
                                            className="cursor-pointer"
                                        />
                                        <Tag color={roleColors[value] || 'default'}>
                                            {getDepartmentMemberRoleType(value)}
                                        </Tag>
                                        <span className="text-xs text-gray-500">
                                            {value === DepartmentMemberRoleType.MEMBER && ''}
                                            {value === DepartmentMemberRoleType.ADMIN && ''}
                                            {value === DepartmentMemberRoleType.SUPER_ADMIN && ''}
                                        </span>
                                    </div>
                                </div>
                            ));
                        })()}
                    </div>
                </div>
            </Modal>
        </>
    );
}
