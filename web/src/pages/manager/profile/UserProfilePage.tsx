import {Avatar, Button, Card, Col, Form, Input, message, Modal, Row, Tabs} from "antd";
import {CameraOutlined, ClockCircleOutlined, LockOutlined, MailOutlined} from "@ant-design/icons";
import {ActionBarComponent} from "../../../components/ActionBarComponent.tsx";
import {useLoggedUser} from "../../../compositions/use-logged-user.ts";
import {formatTimestamp} from "../../../utils/datetime.utils.ts";
import {useEffect, useState} from "react";
import {requestPasswordResetEmailCode, resetPassword, type ResetPasswordDTO} from "../../../api/auth.api.ts";

const { Password } = Input;

const BasicInfo = () => {
    const loggedUser = useLoggedUser();

    const [form] = Form.useForm();

    useEffect(() => {
        if (loggedUser.userProfile) {
            form.setFieldsValue({
                username: loggedUser.userProfile.username,
                nickname: loggedUser.userProfile.nickname,
                email: loggedUser.userProfile.email,
            })
        }
    }, [loggedUser.userProfile]);

    return (
        <div className="py-4">
            <Form
                form={form}
                layout="vertical"
                onFinish={() => message.success('資訊更新成功')}
            >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6">
                    <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
                        <Input className="rounded-xl py-2" disabled />
                    </Form.Item>
                    <Form.Item label="昵称" name="nickname" rules={[{ required: true }]}>
                        <Input className="rounded-xl py-2" />
                    </Form.Item>
                </div>

                <Form.Item
                    label="电子邮箱"
                    name="email"
                    rules={[{ required: true, type: 'email' }]}
                    extra={
                        <p className="mt-2 text-gray-400">若要修改电子邮箱，请前往 <span className="font-bold">账号安全</span> 设置页</p>
                    }
                >
                    <Input className="rounded-xl py-2" disabled />
                </Form.Item>

                <Button type="primary" size="large" className="rounded-xl px-8 h-auto py-2">
                    保存
                </Button>
            </Form>
        </div>
    );
};

const SecuritySettings = () => {
    const loggedUser = useLoggedUser();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [sendingCode, setSendingCode] = useState(false);
    const [countdown, setCountdown] = useState(0);
    const [form] = Form.useForm();

    useEffect(() => {
        if (countdown > 0) {
            const timer = setTimeout(() => {
                setCountdown(countdown - 1);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [countdown]);

    const handleOpenModal = () => {
        form.setFieldsValue({
            email: loggedUser.userProfile?.email,
        });
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        form.resetFields();
        setCountdown(0);
    };

    const handleSendCode = async () => {
        const email = form.getFieldValue('email');
        if (!email) {
            void message.warning('请先输入邮箱');
            return;
        }

        setSendingCode(true);
        try {
            await requestPasswordResetEmailCode(email);
            void message.success('验证码发送成功，请注意查收');
            setCountdown(60);
        } catch (error) {
            // @ts-ignore
            void message.error(`验证码发送失败 ${error.message}`);
        } finally {
            setSendingCode(false);
        }
    };

    const handleSubmit = async (values: ResetPasswordDTO) => {
        setLoading(true);
        try {
            await resetPassword(values);
            void message.success('密码修改成功！');
            handleCloseModal();
        } catch (error) {
            // @ts-ignore
            void message.error(`密码修改失败 ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    const settings = [
        {
            title: '账户密码',
            desc: '建议设置高强度复杂密码以保障账号安全',
            status: '安全性：高',
            action: '修改',
            icon: <LockOutlined className="text-blue-500" />,
            onClick: handleOpenModal,
        },
        {
            title: '电子邮箱',
            desc: loggedUser.userProfile?.email,
            status: '已启用',
            action: '修改',
            icon: <MailOutlined className="text-amber-500" />
        },
    ];

    return (
        <>
            <div className="divide-y divide-slate-100">
                {settings.map((item, idx) => (
                    <div key={idx} className="flex items-center justify-between py-5 first:pt-2 last:pb-2">
                        <div className="flex items-center space-x-4">
                            <div className="flex w-12 h-12 bg-gray-50 rounded-3xl shrink-0 items-center justify-center">{item.icon}</div>

                            <div>
                                <div className="font-semibold text-slate-800">{item.title}</div>
                                <div className="text-sm text-slate-400">{item.desc}</div>
                                <div className="text-xs mt-1 text-slate-500 font-medium">{item.status}</div>
                            </div>
                        </div>
                        <Button type="link" className="font-medium" onClick={item.onClick}>
                            {item.action}
                        </Button>
                    </div>
                ))}
            </div>

            <Modal
                title="修改密码"
                open={isModalOpen}
                onCancel={handleCloseModal}
                footer={null}
                mask={{closable: false}}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    autoComplete="off"
                    className="mt-4"
                >
                    <Form.Item
                        label="电子邮箱"
                        name="email"
                        rules={[
                            { required: true, message: '请输入邮箱' },
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder="电子邮箱"
                            disabled
                        />
                    </Form.Item>

                    <Form.Item
                        label="验证码"
                        name="emailCode"
                        rules={[{ required: true, message: '请输入验证码' }]}
                    >
                        <Row gutter={12}>
                            <Col span={16}>
                                <Input placeholder="验证码" />
                            </Col>
                            <Col span={8}>
                                <Button
                                    type="primary"
                                    loading={sendingCode}
                                    disabled={countdown > 0}
                                    onClick={handleSendCode}
                                    className="w-full"
                                >
                                    {countdown > 0 ? `${countdown}s后重试` : '发送验证码'}
                                </Button>
                            </Col>
                        </Row>
                    </Form.Item>

                    <Form.Item
                        label="新密码"
                        name="newPassword"
                        rules={[
                            { required: true, message: '请输入新密码' },
                            { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: '密码至少8位，且包含数字和字母' },
                            { max: 128, message: '密码长度不能超过128个字符' }
                        ]}
                    >
                        <Password
                            prefix={<LockOutlined className="text-gray-400 mr-2" />}
                            placeholder="新密码"
                        />
                    </Form.Item>

                    <Form.Item
                        label="确认密码"
                        name="confirmPassword"
                        dependencies={['newPassword']}
                        rules={[
                            { required: true, message: '请确认新密码' },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('newPassword') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error('两次输入的密码不一致'));
                                },
                            }),
                        ]}
                    >
                        <Password
                            prefix={<LockOutlined className="text-gray-400 mr-2" />}
                            placeholder="确认新密码"
                        />
                    </Form.Item>

                    <Form.Item className="mb-0">
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={loading}
                            className="w-full"
                        >
                            确认修改
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};

function UserProfileCard() {
    const loggedUser = useLoggedUser();

    return (
        <Card className="rounded-3xl shadow-sm border-none overflow-hidden">
            <div className="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 -m-6 mb-0"></div>
            <div className="relative pt-0 px-6 pb-6">
                <div className="flex justify-center -mt-12 mb-4 relative">
                    <Avatar
                        size={100}
                        className="rounded-3xl border-4 border-white shadow-md"
                        src={loggedUser.userProfile?.avatar}
                    />
                    <Button
                        icon={<CameraOutlined />}
                        className="absolute bottom-0 right-1/2 translate-x-12 rounded-full border-none shadow-lg bg-white text-slate-600 hover:text-blue-500"
                        size="small"
                    />
                </div>
                <div className="text-center mb-6">
                    <h2 className="text-xl font-bold text-slate-800">{loggedUser.userProfile?.nickname}</h2>
                    <p className="text-slate-400 text-sm italic">@{loggedUser.userProfile?.username}</p>
                </div>

                <div className="space-y-4">
                    <div className="flex items-center text-slate-600 text-sm">
                        <MailOutlined className="mr-3 text-slate-300" />
                        {loggedUser.userProfile?.email}
                    </div>
                    <div className="flex items-center text-slate-600 text-sm">
                        <ClockCircleOutlined className="mr-3 text-slate-300" />
                        {formatTimestamp(loggedUser.userProfile?.registeredTime ?? 0)}
                    </div>
                </div>

                {/*<Divider className="my-6 border-slate-100" />

                <div>
                    <div className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">技能表情</div>
                    <div className="flex flex-wrap gap-2">
                        {['React', 'Tailwind', 'AntDesign', 'TypeScript', 'Node.js'].map(tag => (
                            <Tag key={tag} className="m-0 border-none bg-slate-100 text-slate-600 px-3 py-1 rounded-lg text-xs font-medium">
                                {tag}
                            </Tag>
                        ))}
                    </div>
                </div>*/}
            </div>
        </Card>
    )
}

export function UserProfilePage() {

    return (
        <>
            <ActionBarComponent
                title="个人中心"
                subtitle="查看/编辑你的个人资料"
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                <div className="lg:col-span-4">
                    <UserProfileCard />
                </div>

                <div className="lg:col-span-8">
                    <Card className="rounded-3xl shadow-sm border-none min-h-[500px]">
                        <Tabs
                            defaultActiveKey="1"
                            className="modern-tabs"
                            items={[
                                {
                                    key: '1',
                                    label: <span className="px-2 font-medium">基本信息</span>,
                                    children: <BasicInfo />,
                                },
                                {
                                    key: '2',
                                    label: <span className="px-2 font-medium">账号安全</span>,
                                    children: <SecuritySettings />,
                                },
                            ]}
                        />
                    </Card>
                </div>
            </div>
        </>
    )
}