import {Button, Col, Form, Input, message, Modal, Row, Tag, Transfer} from "antd";
import type {Key} from "react";
import {useEffect, useState} from "react";
import {ManagerPageContainer} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateRoleDTO, UserRoleManagerController} from "@/api/user-role.api.ts";
import TextArea from "antd/es/input/TextArea";
import {useUserRoleTableColumns} from "@/components/columns/UserRoleEntityColumns.tsx";
import {getRolePermissions, setRolePermissions} from "@/api/user-role-permission.api.ts";
import {UserPermissionManagerController} from "@/api/user-permission.api.ts";
import {type UserPermission} from "@/types/user-permission.types.ts";
import type {UserRole} from "@/types/user-role.types.ts";
import {useTranslation} from "react-i18next";
import {getPermissionType} from "@/i18n/enum-helpers.ts";

interface TransferItem {
    key: string;
    title: string;
    description: string;
    type: number;
    path?: string | null;
}

export function UserRoleManagerPage() {
    const [allPermissions, setAllPermissions] = useState<UserPermission[]>([]);
    const [selectedRole, setSelectedRole] = useState<UserRole | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Key[]>([]);
    const [isPermissionModalVisible, setIsPermissionModalVisible] = useState(false);
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
        try {
            const res = await getRolePermissions(role.id);
            const ids = res.data?.map(p => String(p.id)) || [];
            setSelectedPermissionIds(ids);
        } catch {
            void message.error(t('pages.userRoleManager.messages.fetchRolePermissionsFailed'));
            setSelectedPermissionIds([]);
        }
    };

    const handleSavePermissions = async () => {
        if (!selectedRole) return;
        const ids = selectedPermissionIds.map(String);
        setSavingPermissions(true);
        try {
            await setRolePermissions(selectedRole.id, ids);
            void message.success(t('pages.userRoleManager.messages.assignSuccess'));
            setIsPermissionModalVisible(false);
        } catch {
            void message.error(t('pages.userRoleManager.messages.assignFailed'));
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

    return (
        <>
            <ManagerPageContainer
                entityName={t('entityNames.userRole')}
                title={t('pages.userRoleManager.title')}
                subtitle={t('pages.userRoleManager.subtitle')}
                columns={columns}
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
            >
                <Transfer
                    dataSource={transferData}
                    titles={[t('pages.userRoleManager.permissionModal.titles.available'), t('pages.userRoleManager.permissionModal.titles.assigned')]}
                    targetKeys={selectedPermissionIds}
                    onChange={handleTransferChange}
                    render={item => {
                        const typeColors: Record<number, string> = {
                            0: 'blue',
                            1: 'green',
                            2: 'purple'
                        };
                        return <span>
                            <Tag color={typeColors[item.type] || 'default'}>{getPermissionType(item.type)}</Tag>
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
