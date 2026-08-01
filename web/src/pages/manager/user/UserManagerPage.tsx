import {Button, Col, Form, Input, message, Popconfirm, Row, Tooltip} from "antd";
import {LogoutOutlined, ReloadOutlined} from "@ant-design/icons";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    forceLogoutUsers,
    type ManagerCreateUserDTO,
    type ManagerReadUserDTO,
    refreshUserAuthorities,
    UserManagerController,
} from "@/api/user/user.api.ts";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useEffect, useRef} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import type {User} from "@/types/user/user.types.ts";

export default function UserManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useUserTableColumns();

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

    return (
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
                    </>
                );
            }}
            query={async (props: ManagerReadUserDTO) => {
                return (await UserManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await UserManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await UserManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await UserManagerController.create(props as ManagerCreateUserDTO)).data!
            }}
        />
    );
}
