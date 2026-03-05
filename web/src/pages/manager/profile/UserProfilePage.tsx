import {Avatar, Button, Card, Col, Form, Input, message, Modal, Row, Tabs, Upload, type UploadProps} from "antd";
import {
    CameraOutlined,
    ClockCircleOutlined,
    LoadingOutlined,
    LockOutlined,
    MailOutlined,
    UserOutlined
} from "@ant-design/icons";
import {ActionBarComponent} from "../../../components/ActionBarComponent.tsx";
import {useLoggedUser} from "../../../compositions/use-logged-user.ts";
import {formatTimestamp} from "../../../utils/datetime.utils.ts";
import {useEffect, useState} from "react";
import {
    requestPasswordResetEmailCode,
    resetPassword,
    requestResetEmailAddressEmailCode,
    resetEmail,
    type ResetPasswordDTO,
    type ResetEmailDTO
} from "../../../api/auth.api.ts";
import {updateUserProfile, type UpdateUserProfileDTO, uploadUserAvatar} from "../../../api/user.api.ts";
import ImageCropper from "../../../components/ImageCropper.tsx";

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

    const [updatingUserProfile, setUpdatingUserProfile] = useState(false);
    const handleSubmitUserProfile = (values: UpdateUserProfileDTO) => {
        setUpdatingUserProfile(true);
        updateUserProfile(values)
            .then(() => {
                void message.success("用户资料更新成功");
                void loggedUser.refreshUserProfile();
            })
            .catch(() => {
                void message.error("用户资料更新失败")
            })
            .finally(() => {
                setUpdatingUserProfile(false);
            })
    }

    return (
        <div className="py-4">
            <Form
                form={form}
                layout="vertical"
                onFinish={handleSubmitUserProfile}
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

                <Button
                    type="primary"
                    size="large"
                    className="rounded-xl px-8 h-auto py-2"
                    loading={updatingUserProfile}
                    onClick={() => { form.submit() }}
                >
                    保存
                </Button>
            </Form>
        </div>
    );
};

const SecuritySettings = () => {
    const loggedUser = useLoggedUser();

    /**
     * Reset Password Modal
     */
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
    const [passwordLoading, setPasswordLoading] = useState(false);
    const [passwordSendingCode, setPasswordSendingCode] = useState(false);
    const [passwordCountdown, setPasswordCountdown] = useState(0);
    const [passwordForm] = Form.useForm();

    useEffect(() => {
        if (passwordCountdown > 0) {
            const timer = setTimeout(() => {
                setPasswordCountdown(passwordCountdown - 1);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [passwordCountdown]);

    const handleOpenPasswordModal = () => {
        passwordForm.setFieldsValue({
            email: loggedUser.userProfile?.email,
        });
        setIsPasswordModalOpen(true);
    };

    const handleClosePasswordModal = () => {
        setIsPasswordModalOpen(false);
        passwordForm.resetFields();
        setPasswordCountdown(0);
    };

    const handleSendPasswordCode = async () => {
        const email = passwordForm.getFieldValue('email');
        if (!email) {
            void message.warning('请先输入邮箱');
            return;
        }

        setPasswordSendingCode(true);
        try {
            await requestPasswordResetEmailCode(email);
            void message.success('验证码发送成功，请注意查收');
            setPasswordCountdown(60);
        } catch (error) {
            // @ts-ignore
            void message.error(`验证码发送失败 ${error.message}`);
        } finally {
            setPasswordSendingCode(false);
        }
    };

    const handlePasswordSubmit = async (values: ResetPasswordDTO) => {
        setPasswordLoading(true);
        try {
            await resetPassword(values);
            void message.success('密码修改成功！');
            handleClosePasswordModal();
        } catch (error) {
            // @ts-ignore
            void message.error(`密码修改失败 ${error.message}`);
        } finally {
            setPasswordLoading(false);
        }
    };

    /**
     * Reset Email Modal
     */
    const [isEmailModalOpen, setIsEmailModalOpen] = useState(false);
    const [emailLoading, setEmailLoading] = useState(false);
    const [emailSendingCode, setEmailSendingCode] = useState(false);
    const [emailCountdown, setEmailCountdown] = useState(0);
    const [emailForm] = Form.useForm();

    useEffect(() => {
        if (emailCountdown > 0) {
            const timer = setTimeout(() => {
                setEmailCountdown(emailCountdown - 1);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [emailCountdown]);

    const handleOpenEmailModal = () => {
        emailForm.setFieldsValue({
            currentEmail: loggedUser.userProfile?.email,
        });
        setIsEmailModalOpen(true);
    };

    const handleCloseEmailModal = () => {
        setIsEmailModalOpen(false);
        emailForm.resetFields();
        setEmailCountdown(0);
    };

    const handleSendEmailCode = async () => {
        const newEmail = emailForm.getFieldValue('newEmail');
        if (!newEmail) {
            void message.warning('请先输入新邮箱');
            return;
        }

        setEmailSendingCode(true);
        try {
            await requestResetEmailAddressEmailCode(newEmail);
            void message.success('验证码发送成功，请注意查收');
            setEmailCountdown(60);
        } catch (error) {
            // @ts-ignore
            void message.error(`验证码发送失败 ${error.message}`);
        } finally {
            setEmailSendingCode(false);
        }
    };

    const handleEmailSubmit = async (values: ResetEmailDTO) => {
        setEmailLoading(true);
        try {
            await resetEmail(values);
            void message.success('邮箱修改成功！');
            handleCloseEmailModal();
            await loggedUser.refreshUserProfile();
        } catch (error) {
            // @ts-ignore
            void message.error(`邮箱修改失败 ${error.message}`);
        } finally {
            setEmailLoading(false);
        }
    };

    /**
     * Account Security Component
     */
    const settings = [
        {
            title: '账户密码',
            desc: '建议设置高强度复杂密码以保障账号安全',
            status: '安全性：高',
            action: '修改',
            icon: <LockOutlined className="text-blue-500" />,
            onClick: handleOpenPasswordModal,
        },
        {
            title: '电子邮箱',
            desc: loggedUser.userProfile?.email,
            status: loggedUser.userProfile?.email ? '已绑定' : '未绑定',
            action: '修改',
            icon: <MailOutlined className="text-amber-500" />,
            onClick: handleOpenEmailModal,
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
                open={isPasswordModalOpen}
                onCancel={handleClosePasswordModal}
                footer={null}
                mask={{closable: false}}
            >
                <Form
                    form={passwordForm}
                    layout="vertical"
                    onFinish={handlePasswordSubmit}
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
                                    loading={passwordSendingCode}
                                    disabled={passwordCountdown > 0}
                                    onClick={handleSendPasswordCode}
                                    className="w-full"
                                >
                                    {passwordCountdown > 0 ? `${passwordCountdown}s后重试` : '发送验证码'}
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
                            loading={passwordLoading}
                            className="w-full"
                        >
                            确认修改
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="修改邮箱"
                open={isEmailModalOpen}
                onCancel={handleCloseEmailModal}
                footer={null}
                mask={{closable: false}}
            >
                <Form
                    form={emailForm}
                    layout="vertical"
                    onFinish={handleEmailSubmit}
                    autoComplete="off"
                    className="mt-4"
                >
                    <Form.Item
                        label="当前邮箱"
                        name="currentEmail"
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder="当前邮箱"
                            disabled
                        />
                    </Form.Item>

                    <Form.Item
                        label="新邮箱"
                        name="newEmail"
                        rules={[
                            { required: true, message: '请输入新邮箱' },
                            { type: 'email', message: '邮箱格式不正确' },
                            { max: 256, message: '邮箱长度不能超过256个字符' }
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder="新邮箱"
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
                                    loading={emailSendingCode}
                                    disabled={emailCountdown > 0}
                                    onClick={handleSendEmailCode}
                                    className="w-full"
                                >
                                    {emailCountdown > 0 ? `${emailCountdown}s后重试` : '发送验证码'}
                                </Button>
                            </Col>
                        </Row>
                    </Form.Item>

                    <Form.Item className="mb-0">
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={emailLoading}
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

    const [isAvatarUploading, setIsAvatarUploading] = useState(false);
    const [cropperOpen, setCropperOpen] = useState(false);
    const [cropperImageUrl, setCropperImageUrl] = useState('');
    const [pendingFile, setPendingFile] = useState<File | null>(null);
    const [cropperKey, setCropperKey] = useState(0);

    const handleAvatarUpload = (file: File) => {
        const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            void message.error('请上传 JPG、PNG、GIF 或 WebP 格式的图片');
            return false;
        }

        const maxSize = 5 * 1024 * 1024;
        if (file.size > maxSize) {
            void message.error('图片大小不能超过 5MB');
            return false;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            setCropperImageUrl(e.target?.result as string);
            setPendingFile(file);
            setCropperKey(prev => prev + 1);
            setCropperOpen(true);
        };
        reader.readAsDataURL(file);

        return false;
    };

    const handleCropConfirm = (croppedBlob: Blob) => {
        setCropperOpen(false);
        setIsAvatarUploading(true);

        const croppedFile = new File([croppedBlob], pendingFile?.name || 'avatar.png', {
            type: 'image/png',
        });

        uploadUserAvatar(croppedFile)
            .then(() => {
                void message.success("头像上传成功");
                void loggedUser.refreshUserProfile();
            })
            .catch(() => {
                void message.error("头像上传失败")
            })
            .finally(() => {
                setIsAvatarUploading(false);
                setPendingFile(null);
                setCropperImageUrl('');
            });
    };

    const handleCropCancel = () => {
        setCropperOpen(false);
        setPendingFile(null);
        setCropperImageUrl('');
    };

    const uploadAvatarProps: UploadProps = {
        name: 'file',
        showUploadList: false,
        beforeUpload: handleAvatarUpload,
        accept: 'image/jpeg,image/png,image/webp',
        customRequest: ({ onSuccess }) => {
            setTimeout(() => {
                onSuccess?.('ok');
            }, 0);
        },
    };

    return (
        <>
            <Card className="rounded-3xl shadow-sm border-none overflow-hidden">
                <div className="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 -m-6 mb-0"></div>
                <div className="relative pt-0 px-6 pb-6">
                    <div className="flex justify-center -mt-12 mb-4 relative">
                        <Avatar
                            size={100}
                            className="rounded-3xl border-4 border-white shadow-md bg-black/50"
                            icon={<UserOutlined />}
                            src={loggedUser.userProfile?.avatar}
                        />
                        <Upload {...uploadAvatarProps}>
                            <Button
                                icon={isAvatarUploading ? <LoadingOutlined /> : <CameraOutlined />}
                                className="absolute bottom-0 right-1/2 translate-x-12 rounded-full border-none shadow-lg
                                         bg-white text-slate-600 transition-all"
                                size="small"
                                loading={isAvatarUploading}
                            />
                        </Upload>
                    </div>
                    <div className="text-center mb-6">
                        <h2 className="text-xl font-bold text-slate-800">{loggedUser.userProfile?.nickname}</h2>
                        <p className="text-slate-400 text-sm italic">@{loggedUser.userProfile?.username}</p>
                    </div>

                    <div className="space-y-4">
                        <div className="flex items-center text-slate-600 text-sm">
                            <MailOutlined className="mr-3 text-slate-300" />
                            {loggedUser.userProfile?.email ?? '未绑定'}
                        </div>
                        <div className="flex items-center text-slate-600 text-sm">
                            <ClockCircleOutlined className="mr-3 text-slate-300" />
                            {formatTimestamp(loggedUser.userProfile?.registeredTime ?? 0)}
                        </div>
                    </div>
                </div>
            </Card>

            <ImageCropper
                key={cropperKey}
                componentKey={cropperKey}
                open={cropperOpen}
                imageUrl={cropperImageUrl}
                onCancel={handleCropCancel}
                onConfirm={handleCropConfirm}
                aspectRatio={1}
                shape="rect"
                title="裁剪头像"
                confirmText="确认上传"
                cancelText="取消"
            />
        </>
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