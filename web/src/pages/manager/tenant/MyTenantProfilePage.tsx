import {
    Avatar,
    Button,
    Card,
    Col,
    Form,
    Input,
    message,
    Row,
    Spin,
    theme,
    Typography,
    Upload,
    type UploadProps
} from "antd";
import {
    CameraOutlined,
    HomeOutlined,
    LoadingOutlined,
    MailOutlined,
    PhoneOutlined,
    ShopOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useEffect, useState} from "react";
import {
    getTenantProfile,
    updateTenantProfile,
    type UpdateTenantProfileDTO,
    uploadTenantIcon
} from "@/api/tenant-profile.api.ts";
import type {TenantProfileVO} from "@/types/tenant.types.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {ImageCropper} from "@/components/ImageCropper.tsx";
const { useToken } = theme;

const { Title, Text } = Typography;
const { TextArea } = Input;

export function MyTenantProfilePage() {
    const { t } = useTranslation();
    const { token } = useToken();

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [tenant, setTenant] = useState<TenantProfileVO | null>(null);

    const [isIconUploading, setIsIconUploading] = useState(false);
    const [cropperOpen, setCropperOpen] = useState(false);
    const [cropperImageUrl, setCropperImageUrl] = useState('');
    const [pendingFile, setPendingFile] = useState<File | null>(null);
    const [cropperKey, setCropperKey] = useState(0);

    useEffect(() => {
        void loadTenantProfile();
    }, []);

    const loadTenantProfile = async () => {
        setLoading(true);
        try {
            const response = await getTenantProfile();
            if (response.data) {
                setTenant(response.data);
                form.setFieldsValue({
                    name: response.data.name,
                    description: response.data.description,
                    contactName: response.data.contactName,
                    contactEmail: response.data.contactEmail,
                    contactPhone: response.data.contactPhone,
                    address: response.data.address,
                });
            }
        } catch (error) {
            void message.error(t('pages.myTenantSettings.loadFailed'));
        } finally {
            setLoading(false);
        }
    };

    const handleIconUpload = (file: File) => {
        const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            void message.error(t('pages.myTenantSettings.avatar.invalidType'));
            return false;
        }

        const maxSize = 5 * 1024 * 1024;
        if (file.size > maxSize) {
            void message.error(t('pages.myTenantSettings.avatar.maxSize'));
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
        setIsIconUploading(true);

        const croppedFile = new File([croppedBlob], pendingFile?.name || 'icon.png', {
            type: 'image/png',
        });

        uploadTenantIcon(croppedFile)
            .then(() => {
                void message.success(t('pages.myTenantSettings.avatar.uploadSuccess'));
                void loadTenantProfile();
            })
            .catch(() => {
                void message.error(t('pages.myTenantSettings.avatar.uploadFailed'));
            })
            .finally(() => {
                setIsIconUploading(false);
                setPendingFile(null);
                setCropperImageUrl('');
            });
    };

    const handleCropCancel = () => {
        setCropperOpen(false);
        setPendingFile(null);
        setCropperImageUrl('');
    };

    const uploadIconProps: UploadProps = {
        name: 'file',
        showUploadList: false,
        beforeUpload: handleIconUpload,
        accept: 'image/jpeg,image/png,image/webp',
        customRequest: ({ onSuccess }) => {
            setTimeout(() => {
                onSuccess?.('ok');
            }, 0);
        },
    };

    const handleSave = async (values: UpdateTenantProfileDTO) => {
        setSaving(true);
        try {
            await updateTenantProfile(values);
            void message.success(t('pages.myTenantSettings.updateSuccess'));
            await loadTenantProfile();
        } catch (error) {
            void message.error(t('pages.myTenantSettings.updateFailed'));
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <Spin size="large" tip={t('pages.myTenantSettings.loading')} />
            </div>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantSettings.title')}
                subtitle={t('pages.myTenantSettings.subtitle')}
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                <div className="lg:col-span-4">
                    <Card className="rounded-2xl shadow-sm border-none overflow-hidden">
                        <div className="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 -m-6 mb-0 dark:from-slate-800 dark:via-blue-900/40 dark:to-slate-800"></div>
                        <div className="relative pt-0 px-6 pb-6">
                            <div className="flex justify-center -mt-12 mb-4 relative">
                                <Avatar
                                    size={100}
                                    className="rounded-2xl border-4 shadow-md bg-white"
                                    icon={<ShopOutlined className="text-4xl text-blue-500" />}
                                    src={tenant?.icon}
                                    style={{ borderColor: token.colorBorderSecondary }}
                                />
                                <Upload {...uploadIconProps}>
                                    <Button
                                        icon={isIconUploading ? <LoadingOutlined /> : <CameraOutlined />}
                                        className="absolute bottom-0 right-1/2 translate-x-12 rounded-full border-none shadow-lg
                                                 bg-white text-slate-600 transition-all"
                                        size="small"
                                        loading={isIconUploading}
                                    />
                                </Upload>
                            </div>
                            <div className="text-center mb-6">
                                <h2 className="text-xl font-bold text-slate-800">{tenant?.name}</h2>
                                <p className="text-slate-400 text-sm mt-1">{t('pages.myTenantDashboard.tenantId')}: {tenant?.tenantId}</p>
                            </div>

                            <div className="space-y-4">
                                <div className="flex items-center text-slate-600 text-sm">
                                    <UserOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">{t('pages.myTenantSettings.form.contactName')}: {tenant?.contactName || t('pages.myTenantDashboard.contact.notSet')}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <MailOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">{tenant?.contactEmail || t('pages.myTenantDashboard.contact.notSet')}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <PhoneOutlined className="mr-3 text-slate-400" />
                                    <span>{tenant?.contactPhone || t('pages.myTenantDashboard.contact.notSet')}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <HomeOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">{tenant?.address || t('pages.myTenantDashboard.contact.notSet')}</span>
                                </div>
                            </div>

                            <div className="mt-6 pt-6 border-t border-slate-100">
                                <div className="grid grid-cols-2 gap-4 text-center">
                                    <div>
                                        <div className="text-xs text-slate-400 mb-1">{t('pages.myTenantDashboard.subscription.subscribedTime')}</div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {tenant?.subscribedTime ? formatTimestamp(Number(tenant.subscribedTime)) : '-'}
                                        </div>
                                    </div>
                                    <div>
                                        <div className="text-xs text-slate-400 mb-1">{t('pages.myTenantDashboard.subscription.expiresTime')}</div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {tenant?.expiresTime ? formatTimestamp(Number(tenant.expiresTime)) : '-'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Card>
                </div>

                <div className="lg:col-span-8">
                    <Card className="rounded-2xl shadow-sm border-none min-h-[500px]">
                        <div className="mb-6">
                            <Title level={4} className="!mb-1">{t('pages.myTenantSettings.basicInfo')}</Title>
                            <Text type="secondary">{t('pages.myTenantSettings.basicInfoDesc')}</Text>
                        </div>

                        <Form
                            form={form}
                            layout="vertical"
                            onFinish={handleSave}
                            className="mt-6"
                        >
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="name"
                                        label={t('pages.myTenantSettings.form.name')}
                                        rules={[
                                            { required: true, message: t('pages.myTenantSettings.validation.nameRequired') },
                                            { max: 64, message: t('pages.myTenantSettings.validation.nameMax') }
                                        ]}
                                    >
                                        <Input
                                            prefix={<ShopOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder={t('pages.myTenantSettings.placeholders.name')}
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="contactName"
                                        label={t('pages.myTenantSettings.form.contactName')}
                                        rules={[
                                            { required: true, message: t('pages.myTenantSettings.validation.contactNameRequired') },
                                            { max: 64, message: t('pages.myTenantSettings.validation.contactNameMax') }
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder={t('pages.myTenantSettings.placeholders.contactName')}
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>

                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="contactEmail"
                                        label={t('pages.myTenantSettings.form.contactEmail')}
                                        rules={[
                                            { required: true, message: t('pages.myTenantSettings.validation.emailRequired') },
                                            { type: 'email', message: t('pages.myTenantSettings.validation.emailInvalid') },
                                            { max: 256, message: t('pages.myTenantSettings.validation.emailMax') }
                                        ]}
                                    >
                                        <Input
                                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder={t('pages.myTenantSettings.placeholders.contactEmail')}
                                            maxLength={256}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="contactPhone"
                                        label={t('pages.myTenantSettings.form.contactPhone')}
                                        rules={[
                                            { required: true, message: t('pages.myTenantSettings.validation.phoneRequired') },
                                            { max: 32, message: t('pages.myTenantSettings.validation.phoneMax') }
                                        ]}
                                    >
                                        <Input
                                            prefix={<PhoneOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder={t('pages.myTenantSettings.placeholders.contactPhone')}
                                            maxLength={32}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>

                            <Form.Item
                                name="address"
                                label={t('pages.myTenantSettings.form.address')}
                                rules={[
                                    { required: true, message: t('pages.myTenantSettings.validation.addressRequired') },
                                    { max: 256, message: t('pages.myTenantSettings.validation.addressMax') }
                                ]}
                            >
                                <Input
                                    prefix={<HomeOutlined className="text-gray-400 mr-2" />}
                                    className="rounded-lg h-10"
                                    placeholder={t('pages.myTenantSettings.placeholders.address')}
                                    maxLength={256}
                                    showCount
                                />
                            </Form.Item>

                            <Form.Item
                                name="description"
                                label={t('pages.myTenantSettings.form.description')}
                                rules={[
                                    { max: 512, message: t('pages.myTenantSettings.validation.descriptionMax') }
                                ]}
                            >
                                <TextArea
                                    className="rounded-lg"
                                    placeholder={t('pages.myTenantSettings.placeholders.description')}
                                    rows={4}
                                    maxLength={512}
                                    showCount
                                />
                            </Form.Item>

                            <Form.Item className="mb-0 mt-8">
                                <Button
                                    type="primary"
                                    size="large"
                                    htmlType="submit"
                                    loading={saving}
                                    className="rounded-xl px-8 h-auto py-2"
                                >
                                    {t('pages.myTenantSettings.buttons.save')}
                                </Button>
                                <Button
                                    size="large"
                                    className="rounded-xl px-8 h-auto py-2 ml-4"
                                    onClick={() => {
                                        form.resetFields();
                                        if (tenant) {
                                            form.setFieldsValue({
                                                name: tenant.name,
                                                description: tenant.description,
                                                contactName: tenant.contactName,
                                                contactEmail: tenant.contactEmail,
                                                contactPhone: tenant.contactPhone,
                                                address: tenant.address,
                                            });
                                        }
                                    }}
                                >
                                    {t('pages.myTenantSettings.buttons.reset')}
                                </Button>
                            </Form.Item>
                        </Form>
                    </Card>
                </div>
            </div>

            <ImageCropper
                key={cropperKey}
                componentKey={cropperKey}
                open={cropperOpen}
                imageUrl={cropperImageUrl}
                onCancel={handleCropCancel}
                onConfirm={handleCropConfirm}
                aspectRatio={1}
                shape="rect"
                title={t('pages.myTenantSettings.avatar.cropTitle')}
                confirmText={t('pages.myTenantSettings.avatar.confirmUpload')}
                cancelText={t('pages.myTenantSettings.avatar.cancel')}
            />
        </>
    );
}
