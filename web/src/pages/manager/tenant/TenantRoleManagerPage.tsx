import {Button, Col, Form, Input, message, Modal, Row} from "antd";
import {useEffect, useRef, useState} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantRoleDTO,
    type ManagerUpdateTenantRoleDTO,
    TenantRoleManagerController
} from "@/api/tenant/rbac/tenant-role.api.ts";
import {useTenantRoleTableColumns} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {TenantRoleIdSelector} from "@/components/selector/TenantRoleIdSelector.tsx";
import {PlusOutlined} from "@ant-design/icons";
import type {TenantRole} from "@/types/tenant/rbac/tenant-role.types.ts";
import {getTenantRolePermissions, setTenantRolePermissions} from "@/api/tenant/rbac/tenant-role-permission.api.ts";
import {TenantPermissionManagerController} from "@/api/tenant/rbac/tenant-permission.api.ts";
import {type TenantPermission} from "@/types/tenant/rbac/tenant-permission.types.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";

export default function TenantRoleManagerPage() {
    const columns = useTenantRoleTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    // Permission assignment modal states
    const [allPermissions, setAllPermissions] = useState<TenantPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<TenantRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<string[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
    const [permissionModalLoading, setPermissionModalLoading] = useState(false);
    const [savingPermissions, setSavingPermissions] = useState(false);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
        pageRef?.current?.refreshData?.({ resetPage: true });
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    const fetchAllPermissions = async () => {
        try {
            const res = await TenantPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error(t('pages.tenantRoleManager.messages.fetchPermissionsFailed'));
        }
    };

    const openAssignPermissionModal = async (role: TenantRole) => {
        setSelectedRole(role);
        setIsPermissionModalVisible(true);
        setPermissionModalLoading(true);
        try {
            const res = await getTenantRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error(t('pages.tenantRoleManager.messages.fetchRolePermissionsFailed'));
            setSelectedPermissionIds([]);
        } finally {
            setPermissionModalLoading(false);
        }
    };

    const handleSavePermissions = async () => {
        if (!selectedRole) return;
        setSavingPermissions(true);
        try {
            await setTenantRolePermissions(selectedRole.id, selectedPermissionIds);
            void message.success(t('pages.tenantRoleManager.messages.assignSuccess'));
            setIsPermissionModalVisible(false);
        } catch {
            void message.error(t('pages.tenantRoleManager.messages.assignFailed'));
        } finally {
            setSavingPermissions(false);
        }
    };

    useEffect(() => {
        void fetchAllPermissions();
    }, []);

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantRoleManager.title')}
                subtitle={t('pages.tenantRoleManager.subtitle')}
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.tenantRoleManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
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
                            label: <span>{t('pages.tenantRoleManager.filter.id')}</span>,
                            children: <Input
                                style={{ width: 160 }}
                                placeholder={t('pages.tenantRoleManager.filter.idPlaceholder')}
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
                                        <input type="hidden" value={selectedTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.tenantRoleManager.modal.name.label')}
                                        rules={[{ required: true, message: t('pages.tenantRoleManager.modal.name.required') }]}
                                    >
                                        <Input
                                            className="w-full rounded-lg h-10"
                                            placeholder={t('pages.tenantRoleManager.modal.name.placeholder')}
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="parentId"
                                        label={t('pages.tenantRoleManager.modal.parentId.label')}
                                    >
                                        <TenantRoleIdSelector tenantId={selectedTenantId || ''} disabledRoleId={editingItem?.id ?? null} />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={24}>
                                    <Form.Item
                                        name="description"
                                        label={t('pages.tenantRoleManager.modal.description.label')}
                                    >
                                        <Input.TextArea
                                            className="w-full rounded-lg"
                                            placeholder={t('pages.tenantRoleManager.modal.description.placeholder')}
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
                            tenantId: selectedTenantId
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
                            tenantId: selectedTenantId
                        };
                        return (await TenantRoleManagerController.create(createProps)).data!
                    }}
                    tableRowActionsRender={(record: TenantRole) => (
                        <Button
                            size="small"
                            onClick={() => openAssignPermissionModal(record)}
                        >
                            {t('pages.tenantRoleManager.action.assignPermission')}
                        </Button>
                    )}
                />
            )}

            <Modal
                title={t('pages.tenantRoleManager.permissionModal.title', { name: selectedRole?.name || '' })}
                open={isPermissionModalVisible}
                onOk={handleSavePermissions}
                onCancel={() => setIsPermissionModalVisible(false)}
                confirmLoading={savingPermissions}
                width={1200}
                centered
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
                styles={{ body: { maxHeight: '65vh', overflowY: 'auto' } }}
            >
                <PermissionTreeTable
                    permissions={allPermissions}
                    loading={permissionModalLoading}
                    typeLabel={getTenantPermissionType}
                    mode="checkbox"
                    selectedIds={selectedPermissionIds}
                    onChange={setSelectedPermissionIds}
                />
            </Modal>
        </>
    )
}
