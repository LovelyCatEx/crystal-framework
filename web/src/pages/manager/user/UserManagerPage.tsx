import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateUserDTO, type ManagerReadUserDTO, UserManagerController} from "@/api/user/user.api.ts";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useRef} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function UserManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useUserTableColumns();

    // Per-field filter state + URL sync (?username=xxx&nickname=yyy)
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            username: 'string',
            nickname: 'string',
        } as const,
    });

    const filterableFields = [
        { field: 'username', type: 'text' as const, label: t('pages.userManager.filter.username') },
        { field: 'email',    type: 'text' as const, label: t('pages.userManager.filter.email') },
        { field: 'nickname', type: 'text' as const, label: t('pages.userManager.filter.nickname') },
    ];

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
                { field: 'username', value: filters.username },
                { field: 'nickname', value: filters.nickname },
            ]}
            tablePrefixActions={[
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
