import {Button, Col, Form, Input, message, Modal, Row} from "antd";
import {useEffect, useRef, useState} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateRoleDTO, UserRoleManagerController} from "@/api/user/rbac/user-role.api.ts";
import TextArea from "antd/es/input/TextArea";
import {useUserRoleTableColumns} from "@/components/columns/UserRoleEntityColumns.tsx";
import {getRolePermissions, setRolePermissions} from "@/api/user/rbac/user-role-permission.api.ts";
import {UserPermissionManagerController} from "@/api/user/rbac/user-permission.api.ts";
import {type UserPermission} from "@/types/user/rbac/user-permission.types.ts";
import type {UserRole} from "@/types/user/rbac/user-role.types.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {getPermissionType} from "@/i18n/enum-helpers.ts";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";

export default function UserRoleManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });
    const [allPermissions, setAllPermissions] = useState<UserPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<string[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
    const [permissionModalLoading, setPermissionModalLoading] = useState(false);
    const [savingPermissions, setSavingPermissions] = useState(false);
    const {t} = useTranslation();
    const columns = useUserRoleTableColumns();

    const fetchAllPermissions = async () => {
        try {
            const res = await UserPermissionManagerController.list();
            setAllPermissions(res.data || []);
        } catch {
            void message.error(t('pages.userRoleManager.messages.fetchPermissionsFailed'));
        }
    };

    const openAssignPermissionModal = async (role: UserRole) => {
        setSelectedRole(role);
        setIsPermissionModalVisible(true);
        setPermissionModalLoading(true);
        try {
            const res = await getRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error(t('pages.userRoleManager.messages.fetchRolePermissionsFailed'));
            setSelectedPermissionIds([]);
        } finally {
            setPermissionModalLoading(false);
        }
    };

    const handleSavePermissions = async () => {
        if (!selectedRole) return;
        setSavingPermissions(true);
        try {
            await setRolePermissions(selectedRole.id, selectedPermissionIds);
            void message.success(t('pages.userRoleManager.messages.assignSuccess'));
            setIsPermissionModalVisible(false);
        } catch {
            void message.error(t('pages.userRoleManager.messages.assignFailed'));
        } finally {
            setSavingPermissions(false);
        }
    };

    useEffect(() => {
        void fetchAllPermissions();
    }, []);

    return (
        <>
            <ManagerPageContainer
                entityName={t('entityNames.userRole')}
                title={t('pages.userRoleManager.title')}
                subtitle={t('pages.userRoleManager.subtitle')}
                columns={columns}
                ref={pageRef}
                searchKeywords={['name', 'description']}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                simpleFilters={[
                    { field: 'id', operator: 'eq', value: filters.id },
                ]}
                tableActions={[
                    {
                        label: <span>{t('pages.userRoleManager.filter.id')}</span>,
                        children: <Input
                            className="rounded-xl"
                            placeholder={t('pages.userRoleManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                        />,
                    },
                ]}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item name="name" label={t('pages.userRoleManager.modal.name.label')} rules={[{ required: true, message: t('pages.userRoleManager.modal.name.required') }, { max: 128, message: t('pages.userRoleManager.modal.name.maxLength') }]}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={128} showCount />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Form.Item name="description" label={t('pages.userRoleManager.modal.description.label')} rules={[{ max: 512, message: t('pages.userRoleManager.modal.description.maxLength') }]}>
                            <TextArea rows={2} placeholder={t('pages.userRoleManager.modal.description.placeholder')} className="rounded-lg" maxLength={512} showCount />
                        </Form.Item>
                    </>
                }
                query={async (props) => {
                    return (await UserRoleManagerController.query(props)).data!
                }}
                delete={async (props) => {
                    return (await UserRoleManagerController.delete(props)).data!
                }}
                update={async (props) => {
                    return (await UserRoleManagerController.update(props)).data!
                }}
                create={async (props) => {
                    return (await UserRoleManagerController.create(props as ManagerCreateRoleDTO)).data!
                }}
                tableRowActionsRender={(record: UserRole) => (
                    <Button
                        size="small"
                        onClick={() => openAssignPermissionModal(record)}
                    >
                        {t('pages.userRoleManager.action.assignPermission')}
                    </Button>
                )}
            />

            <Modal
                title={t('pages.userRoleManager.permissionModal.title', { name: selectedRole?.name || '' })}
                open={isPermissionModalVisible}
                onCancel={() => setIsPermissionModalVisible(false)}
                onOk={handleSavePermissions}
                width={1200}
                centered
                confirmLoading={savingPermissions}
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
                styles={{ body: { maxHeight: '65vh', overflowY: 'auto' } }}
            >
                <PermissionTreeTable
                    permissions={allPermissions}
                    loading={permissionModalLoading}
                    typeLabel={getPermissionType}
                    mode="checkbox"
                    selectedIds={selectedPermissionIds}
                    onChange={setSelectedPermissionIds}
                />
            </Modal>
        </>
    )
}
