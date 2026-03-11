import {Button, Card, Col, Form, Input, message, Modal, Row, Space, Table, Tag, Tree} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantDepartmentIdSelector} from "@/components/selector/TenantDepartmentIdSelector.tsx";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {
    type ManagerCreateTenantDepartmentDTO,
    type ManagerUpdateTenantDepartmentDTO,
    TenantDepartmentManagerController
} from "@/api/tenant-department.api.ts";
import {getDepartmentMembers, setDepartmentMembers as apiSetDepartmentMembers} from "@/api/tenant-department-member.api.ts";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {TENANT_MEMBER_TABLE_COLUMNS} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import {PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleFilled, TeamOutlined, UserAddOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import type {DataNode} from "antd/es/tree";
import type {Key} from "react";
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
    const [editingItem, setEditingItem] = useState<TenantDepartment | null>(null);
    const [saving, setSaving] = useState(false);
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
        } catch {
            void message.error("无法获取部门列表");
        } finally {
            setLoading(false);
        }
    };

    const fetchDepartmentMembers = async (departmentId: string) => {
        setMemberLoading(true);
        try {
            const res = await getDepartmentMembers(departmentId);
            const members = res.data || [];
            setDepartmentMembers(members);
            setTotal(members.length);
            // Store existing member IDs for disabling in selector
            setExistingMemberIds(new Set(members.map(m => String(m.id))));
        } catch {
            void message.error("无法获取部门成员");
        } finally {
            setMemberLoading(false);
        }
    };

    const handleTreeSelect = (selectedKeys: Key[]) => {
        const deptId = selectedKeys[0] as string;
        const dept = departments.find(d => d.id === deptId) || null;
        setSelectedDepartment(dept);
        if (dept) {
            void fetchDepartmentMembers(dept.id);
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
            // Get current member IDs
            const currentIds = departmentMembers.map(m => String(m.id));
            // Add new selected member IDs
            const newIds = selectedMembers.map(m => String(m.id));
            // Merge and remove duplicates
            const allIds = [...new Set([...currentIds, ...newIds])];

            await apiSetDepartmentMembers(selectedDepartment.id, allIds);
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

    const memberColumns = [
        {
            title: "成员ID",
            dataIndex: "id",
            key: "id",
            width: 100,
            render: (_: unknown, row: TenantDepartmentMemberVO) => (
                <CopyableToolTip title={row.member.id}>
                    <Tag color="blue" className="text-xs">{row.member.id}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: "昵称",
            dataIndex: ["user", "nickname"],
            key: "nickname",
            render: (_: unknown, row: TenantDepartmentMemberVO) => row.member.user?.nickname || '-'
        },
        {
            title: "用户名",
            dataIndex: ["user", "username"],
            key: "username",
            render: (_: unknown, row: TenantDepartmentMemberVO) => row.member.user?.username || '-'
        },
        {
            title: "邮箱",
            dataIndex: ["user", "email"],
            key: "email",
            render: (_: unknown, row: TenantDepartmentMemberVO) => row.member.user?.email || '-'
        },
        {
            title: "操作",
            key: "action",
            width: 100,
            render: (_: unknown, row: TenantDepartmentMemberVO) => (
                <Button
                    danger
                    size="small"
                    onClick={async () => {
                        if (!selectedDepartment) return;
                        try {
                            const currentIds = departmentMembers
                                .filter(m => m.id !== row.id)
                                .map(m => String(m.id));
                            await apiSetDepartmentMembers(selectedDepartment.id, currentIds);
                            void message.success("移除成员成功");
                            void fetchDepartmentMembers(selectedDepartment.id);
                        } catch {
                            void message.error("移除成员失败");
                        }
                    }}
                >
                    移除
                </Button>
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
                    <Col span={5}>
                        <Card
                            title="部门列表"
                            className="border-none shadow-sm rounded-2xl overflow-hidden"
                            loading={loading}
                        >
                            <Tree
                                treeData={treeData}
                                onSelect={handleTreeSelect}
                                selectedKeys={selectedDepartment ? [selectedDepartment.id] : []}
                                blockNode
                                showLine
                            />
                        </Card>
                    </Col>

                    {/* Right: Department Details & Members */}
                    <Col span={19}>
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
                                    <p><strong>父部门ID：</strong>{selectedDepartment.parentId || '-'}</p>
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
        </>
    );
}
