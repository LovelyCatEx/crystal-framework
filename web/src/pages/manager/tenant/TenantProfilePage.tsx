import {Avatar, Button, Card, Col, Form, Input, message, Row, Spin, Typography, Upload, type UploadProps} from "antd";
import {
    CameraOutlined,
    HomeOutlined,
    LoadingOutlined,
    MailOutlined,
    PhoneOutlined,
    ShopOutlined,
    UserOutlined
} from "@ant-design/icons";
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

const { Title, Text } = Typography;
const { TextArea } = Input;

export function TenantProfilePage() {
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
            void message.error("加载租户资料失败");
        } finally {
            setLoading(false);
        }
    };

    const handleIconUpload = (file: File) => {
        const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            void message.error('请上传 JPG、PNG 或 WebP 格式的图片');
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
        setIsIconUploading(true);

        const croppedFile = new File([croppedBlob], pendingFile?.name || 'icon.png', {
            type: 'image/png',
        });

        uploadTenantIcon(croppedFile)
            .then(() => {
                void message.success("头像上传成功");
                void loadTenantProfile();
            })
            .catch(() => {
                void message.error("头像上传失败");
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
            void message.success("租户资料更新成功");
            await loadTenantProfile();
        } catch (error) {
            void message.error("租户资料更新失败");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <Spin size="large" tip="加载中..." />
            </div>
        );
    }

    return (
        <>
            <ActionBarComponent
                title="组织设置"
                subtitle="查看和管理租户组织资料"
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                <div className="lg:col-span-4">
                    <Card className="rounded-2xl shadow-sm border-none overflow-hidden">
                        <div className="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 -m-6 mb-0"></div>
                        <div className="relative pt-0 px-6 pb-6">
                            <div className="flex justify-center -mt-12 mb-4 relative">
                                <Avatar
                                    size={100}
                                    className="rounded-2xl border-4 border-white shadow-md bg-white"
                                    icon={<ShopOutlined className="text-4xl text-blue-500" />}
                                    src={tenant?.icon}
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
                                <p className="text-slate-400 text-sm mt-1">租户ID: {tenant?.tenantId}</p>
                            </div>

                            <div className="space-y-4">
                                <div className="flex items-center text-slate-600 text-sm">
                                    <UserOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">联系人: {tenant?.contactName || '未设置'}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <MailOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">{tenant?.contactEmail || '未设置'}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <PhoneOutlined className="mr-3 text-slate-400" />
                                    <span>{tenant?.contactPhone || '未设置'}</span>
                                </div>
                                <div className="flex items-center text-slate-600 text-sm">
                                    <HomeOutlined className="mr-3 text-slate-400" />
                                    <span className="truncate">{tenant?.address || '未设置'}</span>
                                </div>
                            </div>

                            <div className="mt-6 pt-6 border-t border-slate-100">
                                <div className="grid grid-cols-2 gap-4 text-center">
                                    <div>
                                        <div className="text-xs text-slate-400 mb-1">订阅时间</div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {tenant?.subscribedTime ? formatTimestamp(Number(tenant.subscribedTime)) : '-'}
                                        </div>
                                    </div>
                                    <div>
                                        <div className="text-xs text-slate-400 mb-1">过期时间</div>
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
                            <Title level={4} className="!mb-1">基本信息</Title>
                            <Text type="secondary">编辑您的租户组织资料信息</Text>
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
                                        label="租户名称"
                                        rules={[
                                            { required: true, message: '请输入租户名称' },
                                            { max: 64, message: '租户名称长度不能超过64个字符' }
                                        ]}
                                    >
                                        <Input
                                            prefix={<ShopOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder="请输入租户名称"
                                            maxLength={64}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="contactName"
                                        label="联系人姓名"
                                        rules={[
                                            { required: true, message: '请输入联系人姓名' },
                                            { max: 64, message: '联系人姓名长度不能超过64个字符' }
                                        ]}
                                    >
                                        <Input
                                            prefix={<UserOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder="请输入联系人姓名"
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
                                        label="联系人邮箱"
                                        rules={[
                                            { required: true, message: '请输入联系人邮箱' },
                                            { type: 'email', message: '邮箱格式不正确' },
                                            { max: 256, message: '邮箱长度不能超过256个字符' }
                                        ]}
                                    >
                                        <Input
                                            prefix={<MailOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder="请输入联系人邮箱"
                                            maxLength={256}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="contactPhone"
                                        label="联系人电话"
                                        rules={[
                                            { required: true, message: '请输入联系人电话' },
                                            { max: 32, message: '电话长度不能超过32个字符' }
                                        ]}
                                    >
                                        <Input
                                            prefix={<PhoneOutlined className="text-gray-400 mr-2" />}
                                            className="rounded-lg h-10"
                                            placeholder="请输入联系人电话"
                                            maxLength={32}
                                            showCount
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>

                            <Form.Item
                                name="address"
                                label="联系地址"
                                rules={[
                                    { required: true, message: '请输入联系地址' },
                                    { max: 256, message: '地址长度不能超过256个字符' }
                                ]}
                            >
                                <Input
                                    prefix={<HomeOutlined className="text-gray-400 mr-2" />}
                                    className="rounded-lg h-10"
                                    placeholder="请输入联系地址"
                                    maxLength={256}
                                    showCount
                                />
                            </Form.Item>

                            <Form.Item
                                name="description"
                                label="描述"
                                rules={[
                                    { max: 512, message: '描述长度不能超过512个字符' }
                                ]}
                            >
                                <TextArea
                                    className="rounded-lg"
                                    placeholder="请输入租户描述"
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
                                    保存修改
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
                                    重置
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
                title="裁剪头像"
                confirmText="确认上传"
                cancelText="取消"
            />
        </>
    );
}
