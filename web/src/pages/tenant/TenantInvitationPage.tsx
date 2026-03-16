import {useEffect, useState} from 'react';
import {Avatar, Button, Card, Form, Input, message, Tag, Typography} from 'antd';
import {
    ArrowRightOutlined,
    CheckCircleFilled,
    ClockCircleOutlined,
    IdcardOutlined,
    PhoneOutlined,
    SafetyCertificateOutlined,
    ShopOutlined,
    TeamOutlined,
    WarningOutlined
} from '@ant-design/icons';
import {useNavigate, useSearchParams} from "react-router-dom";
import {getTenantProfile} from "@/api/tenant-profile.api.ts";
import {acceptTenantInvitation, queryTenantInvitationByCode} from "@/api/invitation.api.ts";
import type {TenantProfileVO} from "@/types/tenant.types.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {buildDocumentTitle, ProjectDisplayName} from "@/global/global-settings.ts";

const { Title, Text } = Typography;

export function TenantInvitationPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    const [form] = Form.useForm();
    const [inviteCode, setInviteCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [tenantInfo, setTenantInfo] = useState<TenantProfileVO | null>(null);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [expiresAt, setExpiresAt] = useState<string | null>(null);
    const [departmentName, setDepartmentName] = useState<string | null>(null);
    const [reachedUsageLimit, setReachedUsageLimit] = useState(false);
    const [step, setStep] = useState<'input' | 'info' | 'form'>('input');

    useEffect(() => {
        const code = searchParams.get("code");
        if (code) {
            setInviteCode(code);
            setStep('info');
            void fetchTenantInfo(code);
        }
    }, [searchParams]);

    useEffect(() => {
        if (tenantInfo) {
            document.title = buildDocumentTitle(`邀请加入 ${tenantInfo.name}`)
        } else {
            document.title = buildDocumentTitle('组织邀请')
        }
    }, [tenantInfo]);

    const fetchTenantInfo = async (code: string) => {
        const trimmedCode = code.trim()

        if (!trimmedCode) {
            message.warning('请输入邀请码');
            return;
        }

        if (trimmedCode.length < 8) {
            message.error('邀请码长度无效（至少需要 8 位）');
            return;
        }

        setLoading(true);

        try {
            const response = await queryTenantInvitationByCode(trimmedCode);
            if (response.data) {
                setExpiresAt(response.data.expiresAt || null);
                setDepartmentName(response.data.departmentName || null);
                setReachedUsageLimit(response.data.reachedUsageLimit);

                const tenantResponse = await getTenantProfile(response.data.tenantId);
                if (tenantResponse.data) {
                    setTenantInfo(tenantResponse.data);
                    setSearchParams({ code: trimmedCode });
                } else {
                    setTenantInfo(null);
                    message.error('获取组织信息失败');
                }
            } else {
                setTenantInfo(null);
                message.error('邀请码无效');
            }
        } catch (error) {
            setTenantInfo(null);
            message.error('邀请码无效或已过期');
        } finally {
            setLoading(false);
        }
    };

    const handleNextStep = () => {
        if (tenantInfo && !isExpired && !reachedUsageLimit) {
            setStep('form');
        }
    };

    const handleAcceptInvitation = async (values: { realName: string; phoneNumber: string }) => {
        setSubmitting(true);
        try {
            await acceptTenantInvitation({
                invitationCode: inviteCode,
                realName: values.realName,
                phoneNumber: values.phoneNumber,
            });
            setIsSubmitted(true);
            message.success('申请已提交！请等待管理员审核。');
        } catch (error) {
            message.error('提交失败，请重试');
        } finally {
            setSubmitting(false);
        }
    };

    const isExpired = expiresAt ? new Date(Number(expiresAt)) < new Date() : false;

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="w-full max-w-[480px]">
                {!isSubmitted ? (
                    <Card
                        className="border-none shadow-lg rounded-2xl overflow-hidden"
                        styles={{ body: { padding: '40px 32px' } }}
                    >
                        <div className="text-center mb-10">
                            <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-50 rounded-2xl mb-4 text-blue-600">
                                <SafetyCertificateOutlined style={{ fontSize: '32px' }} />
                            </div>
                            <Title level={3} className="!mb-1 font-bold">{tenantInfo?.name ? `加入 ${tenantInfo.name}` : '加入组织'}</Title>
                            <Text type="secondary" className="text-sm">
                                {step === 'input' && '请输入管理员提供的邀请码以继续'}
                                {step === 'info' && '请确认组织信息'}
                                {step === 'form' && '请填写个人信息完成加入'}
                            </Text>
                        </div>

                        {step === 'input' && (
                            <div className="mb-8">
                                <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 ml-1">
                                    组织邀请码
                                </label>
                                <div className="flex gap-2">
                                    <Input
                                        size="large"
                                        placeholder="例如：1brxVqQH2R6c568N"
                                        value={inviteCode}
                                        onChange={(e) => setInviteCode(e.target.value)}
                                        onPressEnter={() => fetchTenantInfo(inviteCode)}
                                        disabled={loading}
                                    />
                                    <Button
                                        type="primary"
                                        size="large"
                                        onClick={() => fetchTenantInfo(inviteCode)}
                                        loading={loading}
                                    >
                                        <ArrowRightOutlined />
                                    </Button>
                                </div>
                            </div>
                        )}

                        {(step === 'info' || step === 'form') && tenantInfo && (
                            <div className="animate-in zoom-in-95 duration-500">
                                <div className="bg-gray-100 rounded-2xl p-5 mb-8">
                                    <div className="flex items-start gap-4">
                                        <Avatar
                                            size={64}
                                            src={tenantInfo.icon}
                                            icon={<ShopOutlined />}
                                        />
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-1">
                                                <span className="font-bold text-lg text-gray-800 truncate leading-none m-0">
                                                    {tenantInfo.name}
                                                </span>
                                                {departmentName && (
                                                    <Tag color="blue">
                                                        <TeamOutlined /> {departmentName}
                                                    </Tag>
                                                )}
                                            </div>
                                            <p className="text-gray-500 text-xs line-clamp-2 mb-0">
                                                {tenantInfo.description
                                                    ? (tenantInfo.description.length > 32
                                                        ? tenantInfo.description.substring(0, 32) + '...'
                                                        : tenantInfo.description)
                                                    : '暂无描述'}
                                            </p>
                                            {reachedUsageLimit && (
                                                <Tag color="warning" className="mt-2">
                                                    <WarningOutlined /> 邀请码已达使用上限
                                                </Tag>
                                            )}
                                            {isExpired && (
                                                <Tag color="error" className="mt-2">
                                                    <ClockCircleOutlined /> 邀请码已过期
                                                </Tag>
                                            )}
                                            {expiresAt && !isExpired && (
                                                <div className="text-xs text-gray-400 mt-2">
                                                    <ClockCircleOutlined /> 有效期至: {formatTimestamp(Number(expiresAt))}
                                                </div>
                                            )}
                                            {!expiresAt && (
                                                <div className="text-xs text-gray-400 mt-2">
                                                    <ClockCircleOutlined /> 永久有效
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                {step === 'info' && (
                                    <>
                                        <Button
                                            type="primary"
                                            block
                                            size="large"
                                            onClick={handleNextStep}
                                            disabled={isExpired || reachedUsageLimit}
                                            loading={loading}
                                            className="mb-4"
                                        >
                                            {isExpired ? '邀请码已过期' : reachedUsageLimit ? '邀请码已达使用上限' : '下一步'}
                                        </Button>
                                        <Button
                                            block
                                            size="large"
                                            onClick={() => {
                                                setStep('input');
                                                setTenantInfo(null);
                                                setInviteCode('');
                                                setSearchParams({});
                                            }}
                                        >
                                            修改邀请码
                                        </Button>
                                    </>
                                )}

                                {step === 'form' && (
                                    <Form
                                        form={form}
                                        layout="vertical"
                                        onFinish={handleAcceptInvitation}
                                        requiredMark={false}
                                    >
                                        <Form.Item
                                            name="realName"
                                            label="真实姓名"
                                            rules={[{ required: true, message: '请输入您的姓名' }]}
                                        >
                                            <Input
                                                prefix={<IdcardOutlined />}
                                                placeholder="在此输入您的真实姓名"
                                                size="large"
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            name="phoneNumber"
                                            label="手机号"
                                            rules={[{ required: true, message: '请输入有效的手机号码' }]}
                                        >
                                            <Input
                                                prefix={<PhoneOutlined />}
                                                placeholder="请输入手机号"
                                                size="large"
                                            />
                                        </Form.Item>

                                        <Button
                                            type="primary"
                                            block
                                            size="large"
                                            htmlType="submit"
                                            loading={submitting}
                                            className="mt-2"
                                        >
                                            接受邀请
                                        </Button>
                                        <Button
                                            block
                                            size="large"
                                            onClick={() => setStep('info')}
                                            className="mt-2"
                                        >
                                            上一步
                                        </Button>
                                    </Form>
                                )}
                            </div>
                        )}
                    </Card>
                ) : (
                    <Card className="border-none shadow-lg rounded-2xl overflow-hidden text-center py-12 px-8">
                        <div className="w-24 h-24 bg-green-50 text-green-500 rounded-full flex items-center justify-center mx-auto mb-8">
                            <CheckCircleFilled style={{ fontSize: '48px' }} />
                        </div>
                        <Title level={2} className="font-bold mb-2">申请已送出！</Title>
                        <p className="text-gray-500 mb-8 max-w-[280px] mx-auto">
                            我们已通知 <b>{tenantInfo?.name}</b> 的管理员。<br />
                            审核通过后您将收到电子邮件通知。
                        </p>
                        <Button
                            size="large"
                            block
                            onClick={() => navigate('/')}
                        >
                            返回首页
                        </Button>
                    </Card>
                )}

                <div className="mt-8 text-center text-gray-400 text-sm">
                    <span className="font-bold">{ProjectDisplayName}</span>
                </div>
            </div>
        </div>
    );
}

export default TenantInvitationPage;
