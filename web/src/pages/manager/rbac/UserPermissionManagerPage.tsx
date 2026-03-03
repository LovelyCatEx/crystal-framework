import {Col, Form, Input, Row, Select, Space, Tag} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreatePermissionDTO,
    type ManagerReadPermissionDTO,
    UserPermissionManagerController
} from "../../../api/user-permission.api.ts";
import React, {type JSX, useEffect, useRef, useState} from "react";
import {PermissionType, type UserPermission} from "../../../types/user-permission.types.ts";
import TextArea from "antd/es/input/TextArea";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";

export function UserPermissionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPermissionType, setFilterPermissionType] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterPermissionType]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="用户权限"
            title="用户权限管理"
            subtitle="配置系统用户权限列表"
            columns={[
                {
                    title: "权限",
                    dataIndex: "id",
                    key: "id",
                    render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
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
                    title: "类型",
                    dataIndex: "type",
                    key: "type",
                    render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={PermissionType[row.type]}>
                                <Tag color="orange" className="text-xs font-mono">{PermissionType[row.type]}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "描述",
                    dataIndex: "description",
                    key: "description",
                    render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.description}>
                                <span className="text-xs font-mono">{row.description}</span>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "资源路径",
                    dataIndex: "path",
                    key: "path",
                    render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.path}>
                                <span color="orange" className="text-xs font-mono">{row.path}</span>
                            </CopyableToolTip>
                        </Space>
                    }
                }
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="name" label="权限名称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="type" label="权限类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择权限类型"
                                    options={[
                                        {
                                            label: 'ACTION',
                                            value: 0,
                                        },
                                        {
                                            label: 'MENU',
                                            value: 1,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="path" label="资源路径">
                                <Input className="w-full rounded-lg h-10 flex items-center" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item name="description" label="权限描述">
                        <TextArea rows={2} placeholder="输入权限描述..." className="rounded-lg" />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadPermissionDTO) => {
                return (await UserPermissionManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await UserPermissionManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await UserPermissionManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await UserPermissionManagerController.create(props as ManagerCreatePermissionDTO)).data!
            }}
            tableActions={[
                {
                    label: <span>类型</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            { value: '0', label: 'ACTION' },
                            { value: '1', label: 'MENU' },
                        ]}
                        onChange={(value) => setFilterPermissionType(Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            type: filterPermissionType === -1 ? undefined : filterPermissionType
                        }
                    }
                }
            ]}
        >

        </ManagerPageContainer>
    )
}