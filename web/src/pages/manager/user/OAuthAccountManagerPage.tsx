import {Col, Form, Input, Row, Select, Space, Tag} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateOAuthAccountDTO,
    type ManagerReadOAuthAccountDTO,
    OAuthAccountManagerController
} from "../../../api/oauth-account.api.ts";
import React, {type JSX, useEffect, useRef, useState} from "react";
import {type OAuthAccount, OAuthPlatform} from "../../../types/oauth-account.types.ts";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";

export function OAuthAccountManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPlatform, setFilterPlatform] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterPlatform]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="OAuth账号"
            title="OAuth账号管理"
            subtitle="管理系统OAuth账号绑定列表"
            columns={[
                {
                    title: "标识",
                    dataIndex: "id",
                    key: "id",
                    render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.identifier}>
                                <span className="text-xs font-mono">{row.identifier}</span>
                            </CopyableToolTip>
                            <CopyableToolTip title={row.id}>
                                <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "平台",
                    dataIndex: "platform",
                    key: "platform",
                    render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={OAuthPlatform[row.platform]}>
                                <Tag color="orange" className="text-xs font-mono">{OAuthPlatform[row.platform]}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "系统用户",
                    dataIndex: "userId",
                    key: "userId",
                    render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                        return <CopyableToolTip title={row.userId ?? '未绑定'}>
                            <Tag color={row.userId ? "green" : "default"} className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                                {row.userId ? `用户ID: ${row.userId}` : '未绑定用户'}
                            </Tag>
                        </CopyableToolTip>
                    }
                },
                {
                    title: "用户信息",
                    dataIndex: "nickname",
                    key: "nickname",
                    render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                        return <Space orientation='horizontal' size={8}>
                            {/* Avatar */}
                            {row.avatar ? (
                                <img src={row.avatar} alt="avatar" className="w-8 h-8 rounded-full" />
                            ) : (
                                <Tag color="default" className="text-xs">无头像</Tag>
                            )}
                            <Space orientation='vertical' size={0}>
                                <CopyableToolTip title={row.nickname ?? ''}>
                                    <span className="text-xs font-mono">昵称: {row.nickname ?? '-'}</span>
                                </CopyableToolTip>
                            </Space>
                        </Space>
                    }
                }
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="userId" label="用户ID">
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="关联的用户ID" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
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
                        <Col span={8}>
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
                return (await OAuthAccountManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await OAuthAccountManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await OAuthAccountManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await OAuthAccountManagerController.create(props as ManagerCreateOAuthAccountDTO)).data!
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
                        }
                    }
                }
            ]}
        >

        </ManagerPageContainer>
    )
}
