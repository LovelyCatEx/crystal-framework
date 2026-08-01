import {Button, Col, DatePicker, Form, Input, message, Modal, Popconfirm, Row, Switch, Tooltip} from "antd";
import {LogoutOutlined, ReloadOutlined, StopOutlined, UnlockOutlined} from "@ant-design/icons";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    banUser,
    forceLogoutUsers,
    type ManagerCreateUserDTO,
    type ManagerReadUserDTO,
    type ManagerUpdateUserDTO,
    refreshUserAuthorities,
    setUserEnabled,
    unbanUser,
    UserManagerController,
} from "@/api/user/user.api.ts";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useEffect, useRef, useState} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import type {User} from "@/types/user/user.types.ts";
import type {Dayjs} from "dayjs";

interface BanFormValues {
    reason: string;
    banUntil?: Dayjs | null;
}

export default function UserManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useUserTableColumns();
    const [banForm] = Form.useForm<BanFormValues>();
    const [banTargetId, setBanTargetId] = useState<string | null>(null);
    const [banSubmitting, setBanSubmitting] = useState(false);

    // Per-field filter state + URL sync (?username=xxx&nickname=yyy)
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            username: 'string',
            nickname: 'string',
            id: 'string',
        } as const,
    });

    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filters.username, filters.nickname, filters.id]);

    const filterableFields = [
        { field: 'id',            type: 'number' as const, label: t('pages.userManager.filter.id') },
        { field: 'username',      type: 'text'   as const, label: t('pages.userManager.filter.username') },
        { field: 'email',         type: 'text'   as const, label: t('pages.userManager.filter.email') },
        { field: 'nickname',      type: 'text'   as const, label: t('pages.userManager.filter.nickname') },
    ];

    const handleRefreshSingleAuthority = async (userId: string) => {
        try {
            await refreshUserAuthorities({ userIds: [userId] });
            void message.success(t('pages.userManager.messages.refreshAuthoritySuccess'));
        } catch {
            void message.error(t('pages.userManager.messages.refreshAuthorityFailed'));
        }
    };

    const handleForceLogoutSingle = async (userId: string) => {
        try {
            await forceLogoutUsers({ userIds: [userId] });
            void message.success(t('pages.userManager.messages.forceLogoutSuccess'));
        } catch {
            void message.error(t('pages.userManager.messages.forceLogoutFailed'));
        }
    };

    const openBanModal = (userId: string) => {
        banForm.resetFields();
        setBanTargetId(userId);
    };

    const handleBanSubmit = async () => {
        const values = await banForm.validateFields();
        if (!banTargetId) return;
        setBanSubmitting(true);
        try {
            await banUser({
                userId: banTargetId,
                reason: values.reason,
                banUntil: values.banUntil ? String(values.banUntil.valueOf()) : null,
            });
            void message.success(t('pages.userManager.messages.banSuccess'));
            setBanTargetId(null);
            pageRef.current?.refreshData();
        } catch {
            void message.error(t('pages.userManager.messages.banFailed'));
        } finally {
            setBanSubmitting(false);
        }
    };

    const handleUnban = async (userId: string) => {
        try {
            await unbanUser({ userId });
            void message.success(t('pages.userManager.messages.unbanSuccess'));
            pageRef.current?.refreshData();
        } catch {
            void message.error(t('pages.userManager.messages.unbanFailed'));
        }
    };

    // Tracks the editing user's original enabled flag so the update handler only calls the dedicated
    // set-enabled endpoint (which carries the force-logout side effect) when the toggle actually changed.
    const editingEnabledRef = useRef<{ id: string; enabled: boolean } | null>(null);

    const handleUpdateUser = async (values: ManagerUpdateUserDTO & { enabledFlag?: boolean }) => {
        const { enabledFlag, ...updateDto } = values;
        const result = await UserManagerController.update(updateDto);
        const original = editingEnabledRef.current;
        if (original && original.id === updateDto.id && enabledFlag !== undefined && enabledFlag !== original.enabled) {
            await setUserEnabled({ userId: updateDto.id, enabled: enabledFlag });
        }
        return result.data!;
    };

    return (
        <>
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.user')}
            title={t('pages.userManager.title')}
            subtitle={t('pages.userManager.subtitle')}
            columns={columns}
            filterableFields={filterableFields}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            searchKeywords={['username', 'email', 'nickname']}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
                { field: 'username', value: filters.username },
                { field: 'nickname', value: filters.nickname },
            ]}
            tableRowActionsRender={(record: User) => (
                <>
                    <Popconfirm
                        title={t('pages.userManager.action.refreshAuthority')}
                        description={<span style={{whiteSpace: 'pre-line'}}>{t('pages.userManager.action.refreshAuthorityConfirm')}</span>}
                        onConfirm={() => handleRefreshSingleAuthority(record.id)}
                        okText={t('components.managerPageContainer.confirm')}
                        cancelText={t('components.managerPageContainer.cancel')}
                    >
                        <Tooltip title={t('pages.userManager.action.refreshAuthority')}>
                            <Button type="text" size="small" icon={<ReloadOutlined />} />
                        </Tooltip>
                    </Popconfirm>
                    <Popconfirm
                        title={t('pages.userManager.action.forceLogout')}
                        description={<span style={{whiteSpace: 'pre-line'}}>{t('pages.userManager.action.forceLogoutConfirm')}</span>}
                        onConfirm={() => handleForceLogoutSingle(record.id)}
                        okText={t('components.managerPageContainer.confirm')}
                        cancelText={t('components.managerPageContainer.cancel')}
                    >
                        <Tooltip title={t('pages.userManager.action.forceLogout')}>
                            <Button type="text" size="small" danger icon={<LogoutOutlined />} />
                        </Tooltip>
                    </Popconfirm>
                    {record.banned ? (
                        <Popconfirm
                            title={t('pages.userManager.action.unban')}
                            description={<span style={{whiteSpace: 'pre-line'}}>{t('pages.userManager.action.unbanConfirm')}</span>}
                            onConfirm={() => handleUnban(record.id)}
                            okText={t('components.managerPageContainer.confirm')}
                            cancelText={t('components.managerPageContainer.cancel')}
                        >
                            <Tooltip title={t('pages.userManager.action.unban')}>
                                <Button type="text" size="small" icon={<UnlockOutlined />} />
                            </Tooltip>
                        </Popconfirm>
                    ) : (
                        <Tooltip title={t('pages.userManager.action.ban')}>
                            <Button type="text" size="small" danger icon={<StopOutlined />} onClick={() => openBanModal(record.id)} />
                        </Tooltip>
                    )}
                </>
            )}
            extraBatchActions={[
                {
                    key: 'refreshAuthority',
                    label: t('pages.userManager.action.refreshAuthority'),
                    confirmTitle: t('pages.userManager.action.refreshAuthority'),
                    confirmContent: <span style={{whiteSpace: 'pre-line'}}>{t('pages.userManager.action.refreshAuthorityBatchConfirm')}</span>,
                    successMessage: t('pages.userManager.messages.refreshAuthoritySuccess'),
                    failedMessage: t('pages.userManager.messages.refreshAuthorityFailed'),
                    handler: (entities) => refreshUserAuthorities({ userIds: entities.map((it) => it.id) }),
                },
                {
                    key: 'forceLogout',
                    label: t('pages.userManager.action.forceLogout'),
                    confirmTitle: t('pages.userManager.action.forceLogout'),
                    confirmContent: <span style={{whiteSpace: 'pre-line'}}>{t('pages.userManager.action.forceLogoutBatchConfirm')}</span>,
                    successMessage: t('pages.userManager.messages.forceLogoutSuccess'),
                    failedMessage: t('pages.userManager.messages.forceLogoutFailed'),
                    handler: (entities) => forceLogoutUsers({ userIds: entities.map((it) => it.id) }),
                },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.userManager.filter.id')}</span>,
                    children: <Input
                        placeholder={t('pages.userManager.filter.idPlaceholder')}
                        defaultValue={filters.id}
                        allowClear
                        className="rounded-xl"
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
                {
                    label: t('pages.userManager.filter.username'),
                    children: (
                        <Input
                            placeholder={t('pages.userManager.filter.username')}
                            defaultValue={filters.username}
                            allowClear
                            className="rounded-xl"
                            onChange={(e) => setFilter('username', e.target.value || undefined)}
                        />
                    ),
                },
                {
                    label: t('pages.userManager.filter.nickname'),
                    children: (
                        <Input
                            placeholder={t('pages.userManager.filter.nickname')}
                            defaultValue={filters.nickname}
                            allowClear
                            className="rounded-xl"
                            onChange={(e) => setFilter('nickname', e.target.value || undefined)}
                        />
                    ),
                },
            ]}
            editModalFormChildren={(editingItem) => {
                const isEditing = !!editingItem;
                editingEnabledRef.current = editingItem
                    ? { id: editingItem.id, enabled: editingItem.enabledFlag }
                    : null;
                return (
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="username"
                                    label={t('pages.userManager.modal.username.label')}
                                    rules={[
                                        { required: true, message: t('pages.userManager.modal.username.required') },
                                        { max: 64, message: t('pages.userManager.modal.username.maxLength') },
                                    ]}
                                >
                                    <Input className="w-full rounded-lg h-10 flex items-center" disabled={isEditing} maxLength={64} showCount />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item
                                    name="nickname"
                                    label={t('pages.userManager.modal.nickname.label')}
                                    rules={[
                                        { required: true, message: t('pages.userManager.modal.nickname.required') },
                                        { max: 32, message: t('pages.userManager.modal.nickname.maxLength') },
                                    ]}
                                >
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={32} showCount />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item
                                    name="email"
                                    label={t('pages.userManager.modal.email.label')}
                                    rules={[{ max: 256, message: t('pages.userManager.modal.email.maxLength') }]}
                                >
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                                </Form.Item>
                            </Col>
                            {!isEditing && (
                                <Col span={12}>
                                    <Form.Item
                                        name="password"
                                        label={t('pages.userManager.modal.password.label')}
                                        rules={[{ required: true, message: t('pages.userManager.modal.password.required') }]}
                                    >
                                        <Input.Password className="w-full rounded-lg h-10 flex items-center" />
                                    </Form.Item>
                                </Col>
                            )}
                        </Row>
                        {isEditing && (
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="enabledFlag"
                                        label={t('pages.userManager.modal.enabled.label')}
                                        extra={t('pages.userManager.modal.enabled.hint')}
                                        valuePropName="checked"
                                    >
                                        <Switch
                                            checkedChildren={t('pages.userManager.modal.enabled.on')}
                                            unCheckedChildren={t('pages.userManager.modal.enabled.off')}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                        )}
                    </>
                );
            }}
            query={async (props: ManagerReadUserDTO) => {
                return (await UserManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await UserManagerController.delete(props)).data!
            }}
            update={handleUpdateUser}
            create={async (props) => {
                return (await UserManagerController.create(props as ManagerCreateUserDTO)).data!
            }}
        />
        <Modal
            title={t('pages.userManager.ban.title')}
            open={banTargetId !== null}
            onOk={handleBanSubmit}
            confirmLoading={banSubmitting}
            onCancel={() => setBanTargetId(null)}
            okText={t('components.managerPageContainer.confirm')}
            cancelText={t('components.managerPageContainer.cancel')}
            destroyOnHidden
        >
            <Form form={banForm} layout="vertical">
                <Form.Item
                    name="reason"
                    label={t('pages.userManager.ban.reasonLabel')}
                    rules={[{ required: true, message: t('pages.userManager.ban.reasonRequired') }]}
                >
                    <Input.TextArea rows={3} maxLength={512} showCount placeholder={t('pages.userManager.ban.reasonPlaceholder')} />
                </Form.Item>
                <Form.Item
                    name="banUntil"
                    label={t('pages.userManager.ban.banUntilLabel')}
                    extra={t('pages.userManager.ban.banUntilHint')}
                >
                    <DatePicker
                        showTime
                        className="w-full"
                        placeholder={t('pages.userManager.ban.banUntilPlaceholder')}
                    />
                </Form.Item>
            </Form>
        </Modal>
        </>
    );
}
