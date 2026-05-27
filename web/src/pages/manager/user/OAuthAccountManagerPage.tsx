import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateOAuthAccountDTO,
    type ManagerReadOAuthAccountDTO,
    OAuthAccountManagerController
} from "@/api/user/oauth-account.api.ts";
import {useEffect, useRef} from "react";
import {useOAuthAccountTableColumns} from "@/components/columns/OAuthAccountEntityColumns.tsx";
import {UserIdSelector} from "@/components/selector";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function OAuthAccountManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: { platform: 'number' }
    });
    const {t} = useTranslation();
    const columns = useOAuthAccountTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.platform]);

    const filterableFields = [
        {
            field: 'platform',
            type: 'number' as const,
            label: t('pages.oauthAccountManager.filter.platform'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.oauthAccountManager.filter.all')}
                    options={[
                        { value: '0', label: 'Github' },
                        { value: '1', label: 'Google' },
                        { value: '2', label: 'QQ' },
                    ]}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.oauthAccount')}
            title={t('pages.oauthAccountManager.title')}
            subtitle={t('pages.oauthAccountManager.subtitle')}
            columns={columns}
            searchKeywords={['identifier', 'nickname', 'email']}
            filterableFields={filterableFields}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'platform', operator: 'eq', value: filters.platform },
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="userId" label={t('pages.oauthAccountManager.modal.userId.label')}>
                                <UserIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="platform" label={t('pages.oauthAccountManager.modal.platform.label')} rules={[{ required: true, message: t('pages.oauthAccountManager.modal.platform.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.oauthAccountManager.modal.platform.placeholder')}
                                    options={[
                                        {
                                            label: 'Github',
                                            value: 0,
                                        },
                                        {
                                            label: 'Google',
                                            value: 1,
                                        },
                                        {
                                            label: 'QQ',
                                            value: 2,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="identifier" label={t('pages.oauthAccountManager.modal.identifier.label')} rules={[{ required: true, message: t('pages.oauthAccountManager.modal.identifier.required') }, { max: 256, message: t('pages.oauthAccountManager.modal.identifier.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.oauthAccountManager.modal.identifier.placeholder')} maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="nickname" label={t('pages.oauthAccountManager.modal.nickname.label')} rules={[{ max: 128, message: t('pages.oauthAccountManager.modal.nickname.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.oauthAccountManager.modal.nickname.placeholder')} maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="avatar" label={t('pages.oauthAccountManager.modal.avatar.label')} rules={[{ max: 256, message: t('pages.oauthAccountManager.modal.avatar.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.oauthAccountManager.modal.avatar.placeholder')} maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            }
            query={async (props: ManagerReadOAuthAccountDTO) => {
                return (await OAuthAccountManagerController.query(props)).data!;
            }}
            delete={async (props) => {
                return (await OAuthAccountManagerController.delete(props)).data!;
            }}
            update={async (props) => {
                return (await OAuthAccountManagerController.update(props)).data!;
            }}
            create={async (props) => {
                return (await OAuthAccountManagerController.create(props as ManagerCreateOAuthAccountDTO)).data!;
            }}
            tableActions={[
                {
                    label: <span>{t('pages.oauthAccountManager.filter.platform')}</span>,
                    children: <Select
                        defaultValue={filters.platform !== undefined ? String(filters.platform) : '-1'}
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.oauthAccountManager.filter.all') },
                            { value: '0', label: 'Github' },
                            { value: '1', label: 'Google' },
                            { value: '2', label: 'QQ' },
                        ]}
                        onChange={(value) => setFilter('platform', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                }
            ]}
        >
        </ManagerPageContainer>
    );
}
