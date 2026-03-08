import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreateUserDTO, UserManagerController} from "@/api/user.api.ts";
import {USER_MANAGER_TABLE_COLUMNS} from "@/components/columns/UserEntityColumns.tsx";

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
                            <Form.Item name="username" label="用户名" rules={[{ required: true }, { max: 64, message: '用户名长度不能超过64个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" disabled maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="nickname" label="昵称" rules={[{ required: true }, { max: 32, message: '昵称长度不能超过32个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="email" label="邮箱" rules={[{ required: false }, { max: 256, message: '邮箱长度不能超过256个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="password" label="密码" rules={[{ required: true }]}>
                                <Input.Password className="w-full rounded-lg h-10 flex items-center" disabled />
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
