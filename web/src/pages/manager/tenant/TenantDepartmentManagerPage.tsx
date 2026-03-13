import {Button, Card, Col, Form, Input, message, Modal, Popover, Row, Space, Table, Tag, Tree} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantDepartmentIdSelector} from "@/components/selector/TenantDepartmentIdSelector.tsx";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {TenantDepartmentPopCard} from "@/components/card/pop/TenantDepartmentPopCard.tsx";
import {
    type ManagerCreateTenantDepartmentDTO,
    type ManagerUpdateTenantDepartmentDTO,
    TenantDepartmentManagerController
} from "@/api/tenant-department.api.ts";
import {
    DepartmentMemberRoleType,
    DepartmentMemberRoleTypeMap,
    type ManagerReadTenantDepartmentMemberDTO,
    TenantDepartmentMemberManagerController
} from "@/api/tenant-department-member.api.ts";
import {departmentMemberRoleTypeToTranslationMap} from "@/i18n/department-member.ts";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {TENANT_MEMBER_TABLE_COLUMNS} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {TENANT_DEPARTMENT_MEMBER_TABLE_COLUMNS} from "@/components/columns/TenantDepartmentMemberEntityColumns.tsx";

import {
    DeleteOutlined,
    EditOutlined,
    ExclamationCircleFilled,
    PlusOutlined,
    SettingOutlined,
    TeamOutlined,
    UserAddOutlined
} from "@ant-design/icons";
import type {Key} from "react";
import {useEffect, useState} from "react";
import type {DataNode} from "antd/es/tree";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import type {TenantDepartmentMemberVO} from "@/types/tenant-department-member.types.ts";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

interface TreeNodeData extends DataNode {
    department: TenantDepartment;
}

export function TenantDepartmentManagerPage() {
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [departments, setDepartments] = useState<TenantDepartment[]>([]);
    const [selectedDepartment, setSelectedDepartment] = useState<TenantDepartment | null>(null);
    const [treeData, setTreeData] = useState<TreeNodeData[]>([]);
    const [loading, setLoading] = useState(false);
    const [memberLoading, setMemberLoading] = useState(false);

    // Members table
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

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
        setSelectedDepartment(null);
        setDepartmentMembers([]);
    };

    const buildTreeData = (depts: TenantDepartment[]): TreeNodeData[] => {
        const map = new Map<string, TreeNodeData>();
        const roots: TreeNodeData[] = [];

        // First pass: create all nodes
        depts.forEach(dept => {
            map.set(dept.id, {
                key: dept.id,
                title: (
                    <Space size="small">
                        <span>{dept.name}</span>
                    </Space>
                ),
                department: dept,
                children: []
            });
        });

        // Second pass: build tree structure
        depts.forEach(dept => {
            const node = map.get(dept.id)!;
            if (dept.parentId && map.has(dept.parentId)) {
                const parent = map.get(dept.parentId)!;
                if (!parent.children) parent.children = [];
                parent.children.push(node);
            } else {
                roots.push(node);
            }
        });

        return roots;
    };

    const fetchDepartments = async (tenantId = selectedTenantId) => {
        if (!tenantId) return;
        setLoading(true);
        try {
            const res = await TenantDepartmentManagerController.list();
            const depts = res.data || [];
            setDepartments(depts);
            setTreeData(buildTreeData(depts));

            // Check URL query parameter for departmentId
            const urlParams = new URLSearchParams(window.location.search);
            const deptIdFromUrl = urlParams.get('departmentId');
            if (deptIdFromUrl) {
                const dept = depts.find(d => d.id === deptIdFromUrl);
                if (dept) {
                    setSelectedDepartment(dept);
                    void fetchDepartmentMembers(dept.id);
                }
            }
        } catch {
            void message.error("无法获取部门列表");
        } finally {
            setLoading(false);
        }
    };

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
            void message.error("无法获取部门成员");
        } finally {
            setMemberLoading(false);
        }
    };

    const updateDepartmentIdInUrl = (departmentId: string | null) => {
        const url = new URL(window.location.href);
        if (departmentId) {
            url.searchParams.set('departmentId', departmentId);
        } else {
            url.searchParams.delete('departmentId');
        }
        window.history.replaceState({}, '', url.toString());
    };

    const handleTreeSelect = (selectedKeys: Key[]) => {
        const deptId = selectedKeys[0] as string;
        const dept = departments.find(d => d.id === deptId) || null;
        setSelectedDepartment(dept);
        if (dept) {
            void fetchDepartmentMembers(dept.id);
            // Update URL query parameter
            updateDepartmentIdInUrl(dept.id);
        } else {
            // Remove departmentId from URL if no department selected
            updateDepartmentIdInUrl(null);
        }
    };

    const openAddModal = () => {
        setEditingItem(null);
        form.resetFields();
        form.setFieldsValue({ tenantId: selectedTenantId });
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
            title: '删除部门',
            icon: <ExclamationCircleFilled />,
            content: `确定要删除部门 "${dept.name}" 吗？`,
            onOk() {
                return TenantDepartmentManagerController.delete({ ids: [dept.id] })
                    .then(() => {
                        void message.success("删除成功");
                        void fetchDepartments();
                        if (selectedDepartment?.id === dept.id) {
                            setSelectedDepartment(null);
                            setDepartmentMembers([]);
                        }
                    })
                    .catch(() => {
                        void message.error("删除失败");
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
                void message.success("更新成功");
            } else {
                await TenantDepartmentManagerController.create(values as ManagerCreateTenantDepartmentDTO);
                void message.success("创建成功");
            }

            setIsEditModalVisible(false);
            void fetchDepartments();
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
            void message.success(`成功添加 ${selectedMembers.length} 名成员`);
            setIsMemberSelectorVisible(false);
            void fetchDepartmentMembers(selectedDepartment.id);
        } catch {
            void message.error("添加成员失败");
        }
    };

    // Check if a member is already in the department
    const isMemberDisabled = (member: TenantMemberVO) => {
        return existingMemberIds.has(String(member.id));
    };

    useEffect(() => {
        if (selectedTenantId) {
            void fetchDepartments(selectedTenantId);
        } else {
            setDepartments([]);
            setTreeData([]);
            setSelectedDepartment(null);
            setDepartmentMembers([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedTenantId]);

    const handleRemoveMember = (row: TenantDepartmentMemberVO) => {
        if (!selectedDepartment) return;
        modal.confirm({
            title: '移除成员',
            icon: <ExclamationCircleFilled />,
            content: `确定要将成员 "${row.member.user?.nickname || row.member.user?.username || row.id}" 从部门中移除吗？`,
            onOk() {
                return TenantDepartmentMemberManagerController.delete({ ids: [row.id] })
                    .then(() => {
                        void message.success("移除成员成功");
                        void fetchDepartmentMembers(selectedDepartment.id);
                    })
                    .catch(() => {
                        void message.error("移除成员失败");
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
            void message.success("更新角色成功");
            setIsRoleEditModalVisible(false);
            void fetchDepartmentMembers(selectedDepartment.id);
        } catch {
            void message.error("更新角色失败");
        } finally {
            setUpdatingRole(false);
        }
    };

    const memberColumns = [
        ...TENANT_DEPARTMENT_MEMBER_TABLE_COLUMNS,
        {
            title: "操作",
            key: "action",
            width: 150,
            render: (_: unknown, row: TenantDepartmentMemberVO) => (
                <Space size="small">
                    <Button
                        size="small"
                        icon={<SettingOutlined />}
                        onClick={() => openRoleEditModal(row)}
                    >
                        编辑角色
                    </Button>
                    <Button
                        danger
                        size="small"
                        onClick={() => handleRemoveMember(row)}
                    >
                        移除
                    </Button>
                </Space>
            )
        }
    ];

    return (
        <>
            {contextHolder}
            <ActionBarComponent
                title="租户部门管理"
                subtitle="管理租户部门及成员"
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={openAddModal}
                        >
                            新增部门
                        </Button>
                    ) : null
                }
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
                <Row gutter={24} className="mt-4">
                    {/* Left: Department Tree */}
                    <Col xs={24} xl={5} className="mb-4 xl:mb-0">
                        <Card
                            title="部门列表"
                            className="border-none shadow-sm rounded-2xl overflow-hidden"
                            loading={loading}
                        >
                            {treeData.length > 0 ? (
                                <Tree
                                    treeData={treeData}
                                    onSelect={handleTreeSelect}
                                    selectedKeys={selectedDepartment ? [selectedDepartment.id] : []}
                                    defaultExpandAll
                                    blockNode
                                    showLine
                                />
                            ) : (
                                <div className="flex flex-col items-center justify-center py-8 text-gray-400">
                                    <TeamOutlined className="text-4xl mb-4" />
                                    <p className="text-sm mb-4">暂无部门</p>
                                    <Button
                                        type="primary"
                                        icon={<PlusOutlined />}
                                        onClick={openAddModal}
                                    >
                                        添加部门
                                    </Button>
                                </div>
                            )}
                        </Card>
                    </Col>

                    {/* Right: Department Details & Members */}
                    <Col xs={24} xl={19}>
                        {selectedDepartment ? (
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
                                                icon={<EditOutlined />}
                                                onClick={() => openEditModal(selectedDepartment)}
                                            >
                                                编辑
                                            </Button>
                                            <Button
                                                danger
                                                icon={<DeleteOutlined />}
                                                onClick={() => handleDelete(selectedDepartment)}
                                            >
                                                删除
                                            </Button>
                                        </Space>
                                    }
                                >
                                    <p><strong>描述：</strong>{selectedDepartment.description || '-'}</p>
                                    <p>
                                        <strong>父部门：</strong>
                                        {selectedDepartment.parentId ? (
                                            <Popover
                                                content={<TenantDepartmentPopCard departmentId={selectedDepartment.parentId} />}
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
                                    <p><strong>创建时间：</strong>{formatTimestamp(selectedDepartment.createdTime)}</p>
                                </Card>

                                {/* Members Table */}
                                <Card
                                    className="border-none shadow-sm rounded-2xl overflow-hidden"
                                    title="部门成员"
                                    extra={
                                        <Button
                                            type="primary"
                                            icon={<UserAddOutlined />}
                                            onClick={openMemberSelector}
                                        >
                                            添加成员
                                        </Button>
                                    }
                                >
                                    <Table
                                        columns={memberColumns}
                                        dataSource={departmentMembers}
                                        rowKey="id"
                                        loading={memberLoading}
                                        scroll={{ x: 1200 }}
                                        pagination={{
                                            current: currentPage,
                                            pageSize: currentPageSize,
                                            total: total,
                                            showSizeChanger: true,
                                            showQuickJumper: true,
                                            showTotal: (total) => `共 ${total} 条`,
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
                        ) : (
                            <Card className="border-none shadow-sm rounded-2xl overflow-hidden h-full flex items-center justify-center">
                                <div className="text-gray-400 text-center">
                                    <TeamOutlined style={{ fontSize: 48 }} />
                                    <p className="mt-4">请从左侧选择一个部门</p>
                                </div>
                            </Card>
                        )}
                    </Col>
                </Row>
            )}

            {/* Edit Modal */}
            <Modal
                title={editingItem ? "编辑部门" : "新增部门"}
                open={isEditModalVisible}
                onOk={handleSave}
                onCancel={() => setIsEditModalVisible(false)}
                confirmLoading={saving}
                width={600}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item name="tenantId" hidden>
                        <Input />
                    </Form.Item>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="name"
                                label="部门名称"
                                rules={[{ required: true, message: '请输入部门名称' }]}
                            >
                                <Input placeholder="输入部门名称" maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="parentId" label="父部门">
                                <TenantDepartmentIdSelector disabledDepartmentId={editingItem?.id ?? null} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea
                            placeholder="输入描述（可选）"
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
                title={`为部门 "${selectedDepartment?.name}" 添加成员`}
                entityName="租户成员"
                columns={TENANT_MEMBER_TABLE_COLUMNS}
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
                title={`编辑成员角色 - ${editingMember?.member.user?.nickname || editingMember?.member.user?.username || ''}`}
                open={isRoleEditModalVisible}
                onOk={handleRoleEditSave}
                onCancel={handleRoleEditCancel}
                confirmLoading={updatingRole}
                width={400}
            >
                <div className="py-4">
                    <p className="mb-4">请选择成员在部门中的角色：</p>
                    <div className="space-y-2">
                        {Object.entries(DepartmentMemberRoleType).map(([key, value]) => (
                            <div
                                key={key}
                                className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                                    editingRoleType === value
                                        ? 'border-blue-500 bg-blue-50'
                                        : 'border-gray-200 hover:border-gray-300'
                                }`}
                                onClick={() => setEditingRoleType(value)}
                            >
                                <div className="flex items-center gap-3">
                                    <input
                                        type="radio"
                                        checked={editingRoleType === value}
                                        onChange={() => setEditingRoleType(value)}
                                        className="cursor-pointer"
                                    />
                                    <Tag color={DepartmentMemberRoleTypeMap[value]?.color || 'default'}>
                                        {departmentMemberRoleTypeToTranslationMap.get(value) || key}
                                    </Tag>
                                    <span className="text-xs text-gray-500">
                                        {value === DepartmentMemberRoleType.MEMBER && ''}
                                        {value === DepartmentMemberRoleType.ADMIN && ''}
                                        {value === DepartmentMemberRoleType.SUPER_ADMIN && ''}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </Modal>
        </>
    );
}
