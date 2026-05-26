import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateUserDTO, type ManagerReadUserDTO, UserManagerController} from "@/api/user/user.api.ts";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useEffect, useRef} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function UserManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter } = useManagerQueryParams({
        schema: { username: 'string', email: 'string', nickname: 'string' }
    });
    const {t} = useTranslation();
    const columns = useUserTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.username, filters.email, filters.nickname]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.user')}
            title={t('pages.userManager.title')}
            subtitle={t('pages.userManager.subtitle')}
            columns={columns}
            editModalFormChildren={(editingItem) => {
                const isEditing = !!editingItem;
                return (
                    <>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item name="username" label={t('pages.userManager.modal.username.label')} rules={[{ required: true, message: t('pages.userManager.modal.username.required') }, { max: 64, message: t('pages.userManager.modal.username.maxLength') }]}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" disabled={isEditing} maxLength={64} showCount />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item name="nickname" label={t('pages.userManager.modal.nickname.label')} rules={[{ required: true, message: t('pages.userManager.modal.nickname.required') }, { max: 32, message: t('pages.userManager.modal.nickname.maxLength') }]}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={32} showCount />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={12}>
                                <Form.Item name="email" label={t('pages.userManager.modal.email.label')} rules={[{ max: 256, message: t('pages.userManager.modal.email.maxLength') }]}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                                </Form.Item>
                            </Col>
                            {!isEditing && (
                                <Col span={12}>
                                    <Form.Item name="password" label={t('pages.userManager.modal.password.label')} rules={[{ required: true, message: t('pages.userManager.modal.password.required') }]}>
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
            extraQueryParams={filters}
            tableActions={[
                {
                    label: <span>{t('pages.userManager.filter.username')}</span>,
                    children: <Input
                        allowClear
                        style={{ width: 180 }}
                        placeholder={t('pages.userManager.filter.usernamePlaceholder')}
                        defaultValue={filters.username as string | undefined}
                        onChange={(e) => setFilter('username', e.target.value || undefined)}
                    />,
                },
                {
                    label: <span>{t('pages.userManager.filter.email')}</span>,
                    children: <Input
                        allowClear
                        style={{ width: 180 }}
                        placeholder={t('pages.userManager.filter.emailPlaceholder')}
                        defaultValue={filters.email as string | undefined}
                        onChange={(e) => setFilter('email', e.target.value || undefined)}
                    />,
                },
                {
                    label: <span>{t('pages.userManager.filter.nickname')}</span>,
                    children: <Input
                        allowClear
                        style={{ width: 180 }}
                        placeholder={t('pages.userManager.filter.nicknamePlaceholder')}
                        defaultValue={filters.nickname as string | undefined}
                        onChange={(e) => setFilter('nickname', e.target.value || undefined)}
                    />,
                },
            ]}
        >

        </ManagerPageContainer>
    )
}
