import {Col, Form, Input, Row, Space, Tag} from "antd";
import {ManagerPageContainer} from "../../../components/ManagerPageContainer.tsx";
import {type ManagerCreateRoleDTO, UserRoleManagerController} from "../../../api/user-role.api.ts";
import React, {type JSX} from "react";
import {type UserRole} from "../../../types/user-role.types.ts";
import TextArea from "antd/es/input/TextArea";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";

export function UserRoleManagerPage() {


    return (
        <ManagerPageContainer
            entityName="用户角色"
            title="用户角色管理"
            subtitle="配置系统用户角色列表"
            columns={[
                {
                    title: "角色",
                    dataIndex: "id",
                    key: "id",
                    render: function (_: unknown, row: UserRole): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.name}>
                                <span className="text-xs font-mono">{row.name}</span>
                            </CopyableToolTip>
                            <CopyableToolTip title={row.id}>
                                <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "描述",
                    dataIndex: "description",
                    key: "description",
                    render: function (_: unknown, row: UserRole): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.description}>
                                <span className="text-xs font-mono">{row.description}</span>
                            </CopyableToolTip>
                        </Space>
                    }
                }
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="角色名称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item name="description" label="角色描述">
                        <TextArea rows={2} placeholder="输入角色描述..." className="rounded-lg" />
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
        >

        </ManagerPageContainer>
    )
}
