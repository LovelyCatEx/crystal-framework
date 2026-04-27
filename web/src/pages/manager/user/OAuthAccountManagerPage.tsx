import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateOAuthAccountDTO,
    type ManagerReadOAuthAccountDTO,
    OAuthAccountManagerController
} from "@/api/oauth-account.api.ts";
import {useEffect, useRef, useState} from "react";
import {useOAuthAccountTableColumns} from "@/components/columns/OAuthAccountEntityColumns.tsx";
import {UserIdSelector} from "@/components/selector";
import {useTranslation} from "react-i18next";

export function OAuthAccountManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPlatform, setFilterPlatform] = useState<number>();
    const {t} = useTranslation();
    const columns = useOAuthAccountTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filterPlatform]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.oauthAccount')}
            title={t('pages.oauthAccountManager.title')}
            subtitle={t('pages.oauthAccountManager.subtitle')}
            columns={columns}
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
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.oauthAccountManager.filter.all') },
                            { value: '0', label: 'Github' },
                            { value: '1', label: 'Google' },
                            { value: '2', label: 'QQ' },
                        ]}
                        onChange={(value) => setFilterPlatform(Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            platform: filterPlatform === -1 ? undefined : filterPlatform
                        };
                    }
                }
            ]}
        >
        </ManagerPageContainer>
    );
}
