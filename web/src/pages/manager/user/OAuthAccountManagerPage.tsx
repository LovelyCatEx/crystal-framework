import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateOAuthAccountDTO,
    type ManagerReadOAuthAccountDTO,
    OAuthAccountManagerController
} from "../../../api/oauth-account.api.ts";
import {useEffect, useRef, useState} from "react";
import {OAUTH_ACCOUNT_MANAGER_TABLE_COLUMNS} from "../../../components/columns/OAuthAccountEntityColumns.tsx";
import {UserIdSelector} from "../../../components/selector/UserIdSelector.tsx";

export function OAuthAccountManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPlatform, setFilterPlatform] = useState<number>();

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterPlatform]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="OAuth账号"
            title="OAuth账号管理"
            subtitle="管理系统OAuth账号绑定列表"
            columns={OAUTH_ACCOUNT_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="userId" label="系统用户">
                                <UserIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择平台"
                                    options={[
                                        {
                                            label: 'GITHUB',
                                            value: 0,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="identifier" label="平台标识" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="平台唯一标识" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="nickname" label="昵称">
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="用户昵称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="avatar" label="头像URL">
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="头像链接" />
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
                    label: <span>平台</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            { value: '0', label: 'GITHUB' },
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
