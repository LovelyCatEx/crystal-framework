import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateRoleDTO, UserRoleManagerController} from "@/api/user-role.api.ts";
import TextArea from "antd/es/input/TextArea";
import {USER_ROLE_MANAGER_TABLE_COLUMNS} from "@/components/columns/UserRoleEntityColumns.tsx";

export function UserRoleManagerPage() {
    return (
        <ManagerPageContainer
            entityName="用户角色"
            title="用户角色管理"
            subtitle="配置系统用户角色列表"
            columns={USER_ROLE_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="角色名称" rules={[{ required: true }, { max: 128, message: '角色名称长度不能超过128个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item name="description" label="角色描述" rules={[{ max: 512, message: '角色描述长度不能超过512个字符' }]}>
                        <TextArea rows={2} placeholder="输入角色描述..." className="rounded-lg" maxLength={512} showCount />
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
