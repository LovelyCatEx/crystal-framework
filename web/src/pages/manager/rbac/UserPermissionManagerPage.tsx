import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreatePermissionDTO,
    type ManagerReadPermissionDTO,
    UserPermissionManagerController
} from "@/api/user-permission.api.ts";
import {useEffect, useRef, useState} from "react";
import TextArea from "antd/es/input/TextArea";
import {USER_PERMISSION_MANAGER_TABLE_COLUMNS} from "@/components/columns/UserPermissionEntityColumns.tsx";

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
            columns={USER_PERMISSION_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="name" label="权限名称" rules={[{ required: true }, { max: 256, message: '权限名称长度不能超过256个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
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
                            <Form.Item name="path" label="资源路径" rules={[{ max: 256, message: '资源路径长度不能超过256个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item name="description" label="权限描述" rules={[{ max: 512, message: '权限描述长度不能超过512个字符' }]}>
                        <TextArea rows={2} placeholder="输入权限描述..." className="rounded-lg" maxLength={512} showCount />
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
