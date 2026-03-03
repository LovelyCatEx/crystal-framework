import {Avatar, Col, Form, Input, Row, Space, Tag} from "antd";
import {ManagerPageContainer} from "../../../components/ManagerPageContainer.tsx";
import {type ManagerCreateUserDTO, UserManagerController} from "../../../api/user.api.ts";
import React, {type JSX} from "react";
import {type User} from "../../../types/user.types.ts";
import {UserOutlined} from "@ant-design/icons";
import {useSWRState} from "../../../compositions/swr.ts";
import {managerGetFileDownloadUrl} from "../../../api/file-resource.api.ts";
import {emptyApiResponseAsync} from "../../../api/system-request.ts";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";
import type {EntityTableColumns} from "../../../components/types/entity-table.types.ts";

function UserAvatar({ fileEntityId }: { fileEntityId?: string | null }) {
    const [avatarUrl] = useSWRState<string | null>(
        fileEntityId ? 'getFileDownloadUrl' : undefined,
        () => fileEntityId ? managerGetFileDownloadUrl(fileEntityId) : emptyApiResponseAsync()
    )

    return <Avatar
        className={avatarUrl ? "" : "bg-black/50"}
        src={avatarUrl ?? <UserOutlined />}
    />
}

export const USER_MANAGER_TABLE_COLUMNS: EntityTableColumns<User> = [
    {
        title: "用户信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='horizontal' size={8}>
                <UserAvatar fileEntityId={row.avatar} />

                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.username}>
                        <span className="text-xs font-mono">@{row.username}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            </Space>
        }
    },
    {
        title: "昵称",
        dataIndex: "nickname",
        key: "nickname",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={8}>
                <CopyableToolTip title={row.nickname}>
                    <span className="text-xs font-mono">{row.nickname}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "邮箱",
        dataIndex: "email",
        key: "email",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.email}>
                    <span className="text-xs font-mono">{row.email}</span>
                </CopyableToolTip>
            </Space>
        }
    }
];

export function UserManagerPage() {


    return (
        <ManagerPageContainer
            entityName="用户"
            title="用户管理"
            subtitle="管理系统用户列表"
            columns={USER_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="nickname" label="昵称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="email" label="邮箱" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="password" label="密码" rules={[{ required: true }]}>
                                <Input.Password className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            }
            query={async (props) => {
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
        >

        </ManagerPageContainer>
    )
}
