import {Button, Col, Form, Input, message, Modal, Row, Spin, Tag, Transfer} from "antd";
import type {Key} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantRoleDTO,
    type ManagerUpdateTenantRoleDTO,
    TenantRoleManagerController
} from "@/api/tenant/rbac/tenant-role.api.ts";
import {useEffect, useRef, useState} from "react";
import {useTenantRoleTableColumns} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {TenantRoleIdSelector} from "@/components/selector/TenantRoleIdSelector.tsx";
import {PlusOutlined} from "@ant-design/icons";
import type {TenantRole} from "@/types/tenant/rbac/tenant-role.types.ts";
import {getTenantRolePermissions, setTenantRolePermissions} from "@/api/tenant/rbac/tenant-role-permission.api.ts";
import {TenantPermissionManagerController} from "@/api/tenant/rbac/tenant-permission.api.ts";
import {type TenantPermission} from "@/types/tenant/rbac/tenant-permission.types.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
    type: number;
    path?: string | null;
}

export default function MyTenantRoleManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    const currentTenantId = currentTenant?.tenantId ?? null;
    const {t} = useTranslation();
    const columns = useTenantRoleTableColumns();

    // Permission assignment modal states
    const [allPermissions, setAllPermissions] = useState<TenantPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<TenantRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
    const [savingPermissions, setSavingPermissions] = useState(false);

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const fetchAllPermissions = async () => {
        try {
            const res = await TenantPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error(t('pages.myTenantRoleManager.messages.fetchPermissionsFailed'));
        }
    };

    const openAssignPermissionModal = async (role: TenantRole) => {
        setSelectedRole(role);
        setIsPermissionModalVisible(true);
        try {
            const res = await getTenantRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error(t('pages.myTenantRoleManager.messages.fetchRolePermissionsFailed'));
            setSelectedPermissionIds([]);
        }
    };

    const handleSavePermissions = async () => {
        if (!selectedRole) return;
        const ids = selectedPermissionIds.map(String);
        setSavingPermissions(true);
        try {
            await setTenantRolePermissions(selectedRole.id, ids);
            void message.success(t('pages.myTenantRoleManager.messages.assignSuccess'));
            setIsPermissionModalVisible(false);
        } catch {
            void message.error(t('pages.myTenantRoleManager.messages.assignFailed'));
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
        type: p.type,
        path: p.path
    }));

    useEffect(() => {
        void fetchAllPermissions();
    }, []);

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantRoleManager.title')} subtitle={t('pages.myTenantRoleManager.subtitle')} />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantRoleManager.title')}
                subtitle={t('pages.myTenantRoleManager.subtitle')}
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.myTenantRoleManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.tenantRole')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    searchKeywords={['name', 'description']}
                    queryParamsSync={syncToUrl}
                    initialQueryValues={initialQueryValues}
                    simpleFilters={[
                        { field: 'id', operator: 'eq', value: filters.id },
                    ]}
                    tableActions={[
                        {
                            label: <span>{t('pages.myTenantRoleManager.filter.id')}</span>,
                            children: <Input
                                style={{ width: 160 }}
                                placeholder={t('pages.myTenantRoleManager.filter.idPlaceholder')}
                                defaultValue={filters.id}
                                allowClear
                                onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                                onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                            />,
                        },
                    ]}
                    editModalFormChildren={(editingItem: TenantRole | null) => (
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={currentTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.myTenantRoleManager.modal.name.label')}
                                        rules={[{ required: true, message: t('pages.myTenantRoleManager.modal.name.required') }]}
                                    >
                                        <Input
                                            className="w-full rounded-lg h-10"
                                            placeholder={t('pages.myTenantRoleManager.modal.name.placeholder')}
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="parentId"
                                        label={t('pages.myTenantRoleManager.modal.parentId.label')}
                                    >
                                        <TenantRoleIdSelector tenantId={currentTenantId || ''} disabledRoleId={editingItem?.id ?? null} />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={24}>
                                    <Form.Item
                                        name="description"
                                        label={t('pages.myTenantRoleManager.modal.description.label')}
                                    >
                                        <Input.TextArea
                                            className="w-full rounded-lg"
                                            placeholder={t('pages.myTenantRoleManager.modal.description.placeholder')}
                                            maxLength={512}
                                            showCount
                                            rows={2}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    )}
                    query={async (props) => {
                        return (await TenantRoleManagerController.query({
                            ...props,
                            tenantId: currentTenantId
                        })).data!
                    }}
                    delete={async (props) => {
                        return (await TenantRoleManagerController.delete(props)).data!
                    }}
                    update={async (props: ManagerUpdateTenantRoleDTO) => {
                        return (await TenantRoleManagerController.update(props)).data!
                    }}
                    create={async (props) => {
                        const createProps: ManagerCreateTenantRoleDTO = {
                            ...(props as ManagerCreateTenantRoleDTO),
                            tenantId: currentTenantId
                        };
                        return (await TenantRoleManagerController.create(createProps)).data!
                    }}
                    tableRowActionsRender={(record: TenantRole) => (
                        <Button
                            size="small"
                            onClick={() => openAssignPermissionModal(record)}
                        >
                            {t('pages.myTenantRoleManager.action.assignPermission')}
                        </Button>
                    )}
                />
            )}

            <Modal
                title={t('pages.myTenantRoleManager.permissionModal.title', { name: selectedRole?.name || '' })}
                open={isPermissionModalVisible}
                onOk={handleSavePermissions}
                onCancel={() => setIsPermissionModalVisible(false)}
                confirmLoading={savingPermissions}
                width={1200}
                centered
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
            >
                <Transfer
                    dataSource={transferData}
                    titles={[t('pages.myTenantRoleManager.permissionModal.titles.available'), t('pages.myTenantRoleManager.permissionModal.titles.assigned')]}
                    targetKeys={selectedPermissionIds}
                    onChange={handleTransferChange}
                    render={item => {
                        const typeColors: Record<number, string> = {
                            0: 'blue',
                            1: 'green'
                        };
                        return <span>
                            <Tag color={typeColors[item.type] || 'default'}>{getTenantPermissionType(item.type)}</Tag>
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
