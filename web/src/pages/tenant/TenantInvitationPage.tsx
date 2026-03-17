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
import {useTranslation} from "react-i18next";

const { Title, Text } = Typography;

export function TenantInvitationPage() {
    const { t } = useTranslation();
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
            document.title = buildDocumentTitle(`${t('pages.tenantInvitation.title')} ${tenantInfo.name}`)
        } else {
            document.title = buildDocumentTitle(t('pages.tenantInvitation.title'))
        }
    }, [tenantInfo, t]);

    const fetchTenantInfo = async (code: string) => {
        const trimmedCode = code.trim()

        if (!trimmedCode) {
            message.warning(t('pages.tenantInvitation.enterInviteCode'));
            return;
        }

        if (trimmedCode.length < 8) {
            message.error(t('pages.tenantInvitation.invalidInviteCodeLength'));
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
                    message.error(t('pages.tenantInvitation.fetchTenantFailed'));
                }
            } else {
                setTenantInfo(null);
                message.error(t('pages.tenantInvitation.invalidInviteCode'));
            }
        } catch (error) {
            setTenantInfo(null);
            message.error(t('pages.tenantInvitation.inviteCodeExpired'));
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
            message.success(t('pages.tenantInvitation.submitSuccess'));
        } catch (error) {
            message.error(t('pages.tenantInvitation.submitFailed'));
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
                            <Title level={3} className="!mb-1 font-bold">{tenantInfo?.name ? `${t('pages.tenantInvitation.title')} ${tenantInfo.name}` : t('pages.tenantInvitation.title')}</Title>
                            <Text type="secondary" className="text-sm">
                                {step === 'input' && t('pages.tenantInvitation.inputStepHint')}
                                {step === 'info' && t('pages.tenantInvitation.infoStepHint')}
                                {step === 'form' && t('pages.tenantInvitation.formStepHint')}
                            </Text>
                        </div>

                        {step === 'input' && (
                            <div className="mb-8">
                                <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 ml-1">
                                    {t('pages.tenantInvitation.inviteCodeLabel')}
                                </label>
                                <div className="flex gap-2">
                                    <Input
                                        size="large"
                                        placeholder={t('pages.tenantInvitation.inviteCodePlaceholder')}
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
                                                    : t('pages.tenantInvitation.noDescription')}
                                            </p>
                                            {reachedUsageLimit && (
                                                <Tag color="warning" className="mt-2">
                                                    <WarningOutlined /> {t('pages.tenantInvitation.reachedUsageLimit')}
                                                </Tag>
                                            )}
                                            {isExpired && (
                                                <Tag color="error" className="mt-2">
                                                    <ClockCircleOutlined /> {t('pages.tenantInvitation.expired')}
                                                </Tag>
                                            )}
                                            {expiresAt && !isExpired && (
                                                <div className="text-xs text-gray-400 mt-2">
                                                    <ClockCircleOutlined /> {t('pages.tenantInvitation.validUntil')}: {formatTimestamp(Number(expiresAt))}
                                                </div>
                                            )}
                                            {!expiresAt && (
                                                <div className="text-xs text-gray-400 mt-2">
                                                    <ClockCircleOutlined /> {t('pages.tenantInvitation.permanentValid')}
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
                                            {isExpired ? t('pages.tenantInvitation.expired') : reachedUsageLimit ? t('pages.tenantInvitation.reachedUsageLimit') : t('pages.tenantInvitation.nextStep')}
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
                                            {t('pages.tenantInvitation.modifyInviteCode')}
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
                                            label={t('pages.tenantInvitation.realName')}
                                            rules={[{ required: true, message: t('pages.tenantInvitation.realNameRequired') }]}
                                        >
                                            <Input
                                                prefix={<IdcardOutlined />}
                                                placeholder={t('pages.tenantInvitation.realNamePlaceholder')}
                                                size="large"
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            name="phoneNumber"
                                            label={t('pages.tenantInvitation.phoneNumber')}
                                            rules={[{ required: true, message: t('pages.tenantInvitation.phoneNumberRequired') }]}
                                        >
                                            <Input
                                                prefix={<PhoneOutlined />}
                                                placeholder={t('pages.tenantInvitation.phoneNumberPlaceholder')}
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
                                            {t('pages.tenantInvitation.acceptInvitation')}
                                        </Button>
                                        <Button
                                            block
                                            size="large"
                                            onClick={() => setStep('info')}
                                            className="mt-2"
                                        >
                                            {t('pages.tenantInvitation.previousStep')}
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
                        <Title level={2} className="font-bold mb-2">{t('pages.tenantInvitation.submittedTitle')}</Title>
                        <p className="text-gray-500 mb-8 max-w-[280px] mx-auto">
                            {t('pages.tenantInvitation.submittedDescription', { tenantName: tenantInfo?.name })}<br />
                        </p>
                        <Button
                            size="large"
                            block
                            onClick={() => navigate('/')}
                        >
                            {t('pages.tenantInvitation.backToHome')}
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
