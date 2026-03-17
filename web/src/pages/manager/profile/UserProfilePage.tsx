import {
    Avatar,
    Button,
    Card,
    Col,
    Form,
    Input,
    message,
    Modal,
    Row,
    Space,
    Tabs,
    theme,
    Upload,
    type UploadProps
} from "antd";
import {
    CameraOutlined,
    ClockCircleOutlined,
    ExclamationCircleFilled,
    LoadingOutlined,
    LockOutlined,
    MailOutlined,
    PlusOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useMemo, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {
    requestPasswordResetEmailCode,
    requestResetEmailAddressEmailCode,
    resetEmail,
    type ResetEmailDTO,
    resetPassword,
    type ResetPasswordDTO,
    unbindOAuthAccount
} from "@/api/auth.api.ts";
import {updateUserProfile, type UpdateUserProfileDTO, uploadUserAvatar} from "@/api/user.api.ts";
import {ImageCropper} from "@/components/ImageCropper.tsx";
import {useSWRState} from "@/compositions/swr.ts";
import {getUserOAuthAccounts} from "@/api/user-oauth.api.ts";
import PlatformIcon from "../../../components/PlatformIcon.tsx";
import type {UserOAuthAccountVO} from "@/types/user-oauth.types.ts";
import {OAuthPlatform} from "@/types/oauth-account.types.ts";
import {getOAuth2LoginUrl} from "@/utils/oauth2.ts";
import {PLATFORM_REGISTRATION_ID_MAP} from "@/global/constants.ts";
const { useToken } = theme;

const { Password } = Input;

const BasicInfo = () => {
    const { t } = useTranslation();
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
    }, [loggedUser.userProfile, form]);

    const [updatingUserProfile, setUpdatingUserProfile] = useState(false);
    const handleSubmitUserProfile = (values: UpdateUserProfileDTO) => {
        setUpdatingUserProfile(true);
        updateUserProfile(values)
            .then(() => {
                void message.success(t('pages.userProfile.basicInfo.updateSuccess'));
                void loggedUser.refreshUserProfile();
            })
            .catch(() => {
                void message.error(t('pages.userProfile.basicInfo.updateFailed'))
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
                    <Form.Item label={t('pages.userProfile.basicInfo.username')} name="username" rules={[{ required: true }]}>
                        <Input className="rounded-xl py-2" disabled />
                    </Form.Item>
                    <Form.Item label={t('pages.userProfile.basicInfo.nickname')} name="nickname" rules={[{ required: true }]}>
                        <Input className="rounded-xl py-2" />
                    </Form.Item>
                </div>

                <Form.Item
                    label={t('pages.userProfile.basicInfo.email')}
                    name="email"
                    rules={[{ required: true, type: 'email' }]}
                    extra={
                        <p className="mt-2 text-gray-400">{t('pages.userProfile.basicInfo.emailHint')}</p>
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
                    {t('pages.userProfile.basicInfo.save')}
                </Button>
            </Form>
        </div>
    );
};

const SecuritySettings = () => {
    const { t } = useTranslation();
    const loggedUser = useLoggedUser();

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
            void message.warning(t('pages.userProfile.security.passwordModal.emailRequired'));
            return;
        }

        setPasswordSendingCode(true);
        try {
            await requestPasswordResetEmailCode(email);
            void message.success(t('pages.userProfile.security.passwordModal.codeSendSuccess'));
            setPasswordCountdown(60);
        } catch (error) {
            void message.error(t('pages.userProfile.security.passwordModal.codeSendFailed'));
        } finally {
            setPasswordSendingCode(false);
        }
    };

    const handlePasswordSubmit = async (values: ResetPasswordDTO) => {
        setPasswordLoading(true);
        try {
            await resetPassword(values);
            void message.success(t('pages.userProfile.security.passwordModal.updateSuccess'));
            handleClosePasswordModal();
        } catch (error) {
            void message.error(t('pages.userProfile.security.passwordModal.codeSendFailed'));
        } finally {
            setPasswordLoading(false);
        }
    };

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
            void message.warning(t('pages.userProfile.security.emailModal.newEmailRequired'));
            return;
        }

        setEmailSendingCode(true);
        try {
            await requestResetEmailAddressEmailCode(newEmail);
            void message.success(t('pages.userProfile.security.emailModal.codeSendSuccess'));
            setEmailCountdown(60);
        } catch (error) {
            void message.error(t('pages.userProfile.security.emailModal.codeSendFailed'));
        } finally {
            setEmailSendingCode(false);
        }
    };

    const handleEmailSubmit = async (values: ResetEmailDTO) => {
        setEmailLoading(true);
        try {
            await resetEmail(values);
            void message.success(t('pages.userProfile.security.emailModal.updateSuccess'));
            handleCloseEmailModal();
            await loggedUser.refreshUserProfile();
        } catch (error) {
            void message.error(t('pages.userProfile.security.emailModal.codeSendFailed'));
        } finally {
            setEmailLoading(false);
        }
    };

    const settings = [
        {
            title: t('pages.userProfile.security.accountPassword.title'),
            desc: t('pages.userProfile.security.accountPassword.desc'),
            status: t('pages.userProfile.security.accountPassword.status'),
            action: t('pages.userProfile.security.accountPassword.action'),
            icon: <LockOutlined className="text-blue-500" />,
            onClick: handleOpenPasswordModal,
        },
        {
            title: t('pages.userProfile.security.email.title'),
            desc: loggedUser.userProfile?.email,
            status: loggedUser.userProfile?.email ? t('pages.userProfile.security.email.statusBound') : t('pages.userProfile.security.email.statusUnbound'),
            action: t('pages.userProfile.security.email.action'),
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
                title={t('pages.userProfile.security.passwordModal.title')}
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
                        label={t('pages.userProfile.security.passwordModal.email')}
                        name="email"
                        rules={[{ required: true }]}
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder={t('pages.userProfile.security.passwordModal.email')}
                            disabled
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('pages.userProfile.security.passwordModal.verificationCode')}
                        name="emailCode"
                        rules={[{ required: true, message: t('pages.userProfile.security.passwordModal.verificationCode') }]}
                    >
                        <Row gutter={12}>
                            <Col span={16}>
                                <Input placeholder={t('pages.userProfile.security.passwordModal.verificationCode')} />
                            </Col>
                            <Col span={8}>
                                <Button
                                    type="primary"
                                    loading={passwordSendingCode}
                                    disabled={passwordCountdown > 0}
                                    onClick={handleSendPasswordCode}
                                    className="w-full"
                                >
                                    {passwordCountdown > 0 ? t('pages.userProfile.security.passwordModal.resendCode', { seconds: passwordCountdown }) : t('pages.userProfile.security.passwordModal.sendCode')}
                                </Button>
                            </Col>
                        </Row>
                    </Form.Item>

                    <Form.Item
                        label={t('pages.userProfile.security.passwordModal.newPassword')}
                        name="newPassword"
                        rules={[
                            { required: true, message: t('pages.userProfile.security.passwordModal.newPassword') },
                            { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: t('pages.userProfile.security.passwordModal.passwordHint') },
                            { max: 128, message: 'Password max 128 chars' }
                        ]}
                    >
                        <Password
                            prefix={<LockOutlined className="text-gray-400 mr-2" />}
                            placeholder={t('pages.userProfile.security.passwordModal.newPassword')}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('pages.userProfile.security.passwordModal.confirmPassword')}
                        name="confirmPassword"
                        dependencies={['newPassword']}
                        rules={[
                            { required: true, message: t('pages.userProfile.security.passwordModal.confirmPassword') },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('newPassword') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error('Password mismatch'));
                                },
                            }),
                        ]}
                    >
                        <Password
                            prefix={<LockOutlined className="text-gray-400 mr-2" />}
                            placeholder={t('pages.userProfile.security.passwordModal.confirmPassword')}
                        />
                    </Form.Item>

                    <Form.Item className="mb-0">
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={passwordLoading}
                            className="w-full"
                        >
                            {t('pages.userProfile.security.passwordModal.confirm')}
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title={t('pages.userProfile.security.emailModal.title')}
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
                        label={t('pages.userProfile.security.emailModal.currentEmail')}
                        name="currentEmail"
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder={t('pages.userProfile.security.emailModal.currentEmail')}
                            disabled
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('pages.userProfile.security.emailModal.newEmail')}
                        name="newEmail"
                        rules={[
                            { required: true, message: t('pages.userProfile.security.emailModal.newEmailRequired') },
                            { type: 'email', message: 'Invalid email' },
                            { max: 256, message: 'Email max 256 chars' }
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                            placeholder={t('pages.userProfile.security.emailModal.newEmail')}
                        />
                    </Form.Item>

                    <Form.Item
                        label={t('pages.userProfile.security.emailModal.verificationCode')}
                        name="emailCode"
                        rules={[{ required: true, message: t('pages.userProfile.security.emailModal.verificationCode') }]}
                    >
                        <Row gutter={12}>
                            <Col span={16}>
                                <Input placeholder={t('pages.userProfile.security.emailModal.verificationCode')} />
                            </Col>
                            <Col span={8}>
                                <Button
                                    type="primary"
                                    loading={emailSendingCode}
                                    disabled={emailCountdown > 0}
                                    onClick={handleSendEmailCode}
                                    className="w-full"
                                >
                                    {emailCountdown > 0 ? t('pages.userProfile.security.emailModal.resendCode', { seconds: emailCountdown }) : t('pages.userProfile.security.emailModal.sendCode')}
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
                            {t('pages.userProfile.security.emailModal.confirm')}
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};

const OAuthAccountSettings = () => {
    const { t } = useTranslation();
    const [modal, contextHolder] = Modal.useModal();

    const loggedUser = useLoggedUser();

    const [accounts, , , updateAccounts] = useSWRState('/getUserOAuthAccounts', getUserOAuthAccounts)

    const handleUnbind = (account: UserOAuthAccountVO) => {
        modal.confirm({
            title: t('pages.userProfile.oauth.unbindTitle'),
            icon: <ExclamationCircleFilled />,
            content: t('pages.userProfile.oauth.unbindConfirm', { nickname: account.nickname }),
            onOk() {
                unbindOAuthAccount({ oauthAccountId: account.id })
                    .then(() => {
                        void message.success(t('pages.userProfile.oauth.unbindSuccess'));
                        void loggedUser.refreshUserProfile();
                        void updateAccounts();
                    })
                    .catch(() => {
                        void message.warning(t('pages.userProfile.oauth.unbindFailed'));
                    })
            },
        });
    }

    const handleBind = (platform: OAuthPlatform) => {
        window.location.href = getOAuth2LoginUrl(PLATFORM_REGISTRATION_ID_MAP[platform]);
    }

    const allPlatforms = useMemo(() =>
        [OAuthPlatform.GITHUB, OAuthPlatform.GOOGLE, OAuthPlatform.OICQ]
    , []);

    const boundPlatformIds = useMemo(() => new Set(accounts?.map(a => a.platformId) ?? []), [accounts]);

    const boundAccounts = useMemo(() => {
        return accounts?.map(account => ({
            ...account,
            platform: account.platformId as OAuthPlatform
        })) ?? [];
    }, [accounts]);

    const unboundPlatforms = useMemo(() => {
        return allPlatforms.filter(platform => !boundPlatformIds.has(platform));
    }, [allPlatforms, boundPlatformIds]);

    return (
        <>
            <div className="space-y-4">
                {boundAccounts.map((account) => (
                    <div key={account.id} className="flex flex-row items-center space-x-2 justify-between py-3 border-b border-gray-100 last:border-b-0">
                        <Space orientation="horizontal" size={12}>
                            <Avatar
                                size={40}
                                icon={<UserOutlined />}
                                src={account.avatar}
                            />

                            <Space orientation="vertical" size={0}>
                                <span className="font-medium">{account.nickname}</span>
                                <PlatformIcon platform={account.platform} />
                            </Space>
                        </Space>

                        <Button type="link" className="font-medium text-red-500" onClick={() => { handleUnbind(account); }}>
                            {t('pages.userProfile.oauth.bind')}
                        </Button>
                    </div>
                ))}

                {unboundPlatforms.length > 0 && (
                    <div className="pt-4">
                        <div className="text-sm text-gray-500 mb-3">{t('pages.userProfile.oauth.availablePlatforms')}</div>
                        <div className="flex flex-wrap gap-3">
                            {unboundPlatforms.map((platform) => (
                                <Button
                                    key={platform}
                                    type="dashed"
                                    className="flex items-center gap-2 px-4 py-2 h-auto"
                                    onClick={() => handleBind(platform)}
                                >
                                    <PlusOutlined />
                                    <PlatformIcon platform={platform} />
                                </Button>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {contextHolder}
        </>
    );
}

function UserProfileCard() {
    const { t } = useTranslation();
    const { token } = useToken();

    const loggedUser = useLoggedUser();

    const [isAvatarUploading, setIsAvatarUploading] = useState(false);
    const [cropperOpen, setCropperOpen] = useState(false);
    const [cropperImageUrl, setCropperImageUrl] = useState('');
    const [pendingFile, setPendingFile] = useState<File | null>(null);
    const [cropperKey, setCropperKey] = useState(0);

    const handleAvatarUpload = (file: File) => {
        const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            void message.error(t('pages.userProfile.avatar.invalidType'));
            return false;
        }

        const maxSize = 5 * 1024 * 1024;
        if (file.size > maxSize) {
            void message.error(t('pages.userProfile.avatar.maxSize'));
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
                void message.success(t('pages.userProfile.avatar.uploadSuccess'));
                void loggedUser.refreshUserProfile();
            })
            .catch(() => {
                void message.error(t('pages.userProfile.avatar.uploadFailed'))
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
            <Card className="rounded-2xl shadow-sm border-none overflow-hidden">
                <div className="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 -m-6 mb-0 dark:from-slate-800 dark:via-blue-900/40 dark:to-slate-800"></div>
                <div className="relative pt-0 px-6 pb-6">
                    <div className="flex justify-center -mt-12 mb-4 relative">
                        <Avatar
                            size={100}
                            className="rounded-3xl border-4 shadow-md bg-black/50"
                            icon={<UserOutlined />}
                            src={loggedUser.userProfile?.avatar}
                            style={{ borderColor: token.colorBorderSecondary }}
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
                            {loggedUser.userProfile?.email ?? t('pages.userProfile.card.unbound')}
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
                title={t('pages.userProfile.avatar.cropTitle')}
                confirmText={t('pages.userProfile.avatar.confirmUpload')}
                cancelText={t('pages.userProfile.avatar.cancel')}
            />
        </>
    )
}

export function UserProfilePage() {
    const { t } = useTranslation();
    
    const TAB_KEYS = {
        INFO: 'info',
        SECURITY: 'security',
        OAUTH: 'oauth'
    } as const;

    type TabKey = typeof TAB_KEYS[keyof typeof TAB_KEYS];

    const [searchParams, setSearchParams] = useSearchParams();

    const activeTab = (searchParams.get('tab') as TabKey) || TAB_KEYS.INFO;

    const handleTabChange = (key: string) => {
        setSearchParams({ tab: key });
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.userProfile.title')}
                subtitle={t('pages.userProfile.subtitle')}
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                <div className="lg:col-span-4">
                    <UserProfileCard />
                </div>

                <div className="lg:col-span-8">
                    <Card className="rounded-2xl shadow-sm border-none min-h-[500px]">
                        <Tabs
                            activeKey={activeTab}
                            className="modern-tabs"
                            items={[
                                {
                                    key: TAB_KEYS.INFO,
                                    label: <span className="px-2 font-medium">{t('pages.userProfile.tabs.basicInfo')}</span>,
                                    children: <BasicInfo />,
                                },
                                {
                                    key: TAB_KEYS.SECURITY,
                                    label: <span className="px-2 font-medium">{t('pages.userProfile.tabs.security')}</span>,
                                    children: <SecuritySettings />,
                                },
                                {
                                    key: TAB_KEYS.OAUTH,
                                    label: <span className="px-2 font-medium">{t('pages.userProfile.tabs.oauth')}</span>,
                                    children: <OAuthAccountSettings />,
                                },
                            ]}
                            onChange={handleTabChange}
                        />
                    </Card>
                </div>
            </div>
        </>
    )
}
