import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Button, Card, Col, Form, Input, message, Row, Spin, Steps, Typography} from 'antd';
import {CheckCircleOutlined, LockOutlined, MailOutlined, UserOutlined} from '@ant-design/icons';
import {useTranslation} from 'react-i18next';
import {ProjectDisplayName} from '@/global/global-settings.ts';
import {LanguageSwitcher} from '@/components/LanguageSwitcher.tsx';
import {checkSystemInitialized, initializeSystem} from '@/api/system-initialize.api.ts';

const { Password } = Input;
const { Title, Text } = Typography;

interface AdminAccountFormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

interface MailServerFormData {
  host: string;
  port: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
}

export function SystemInitializePage() {
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [checkingStatus, setCheckingStatus] = useState(true);
  const [adminForm] = Form.useForm<AdminAccountFormData>();
  const [mailForm] = Form.useForm<MailServerFormData>();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [adminData, setAdminData] = useState<AdminAccountFormData | null>(null);

  useEffect(() => {
    const checkStatus = async () => {
      try {
        const response = await checkSystemInitialized();
        if (response.data?.initialized) {
          navigate('/');
        }
      } catch {
        navigate('/');
      } finally {
        setCheckingStatus(false);
      }
    };

    void checkStatus();
  }, [navigate]);

  const steps = [
    { title: t('pages.systemInitializePage.steps.adminAccount'), description: t('pages.systemInitializePage.steps.adminAccountDesc') },
    { title: t('pages.systemInitializePage.steps.mailServer'), description: t('pages.systemInitializePage.steps.mailServerDesc') },
    { title: t('pages.systemInitializePage.steps.complete'), description: t('pages.systemInitializePage.steps.completeDesc') },
  ];

  const handleAdminSubmit = async (values: AdminAccountFormData) => {
    setAdminData(values);
    setCurrentStep(1);
  };

  const handleMailSubmit = async (values: MailServerFormData) => {
    if (!adminData) {
      void message.error(t('pages.systemInitializePage.adminAccount.messages.failed'));
      return;
    }

    setLoading(true);
    try {
      await initializeSystem({
        username: adminData.username,
        password: adminData.password,
        email: adminData.email,
        smtpHost: values.host,
        smtpPort: Number(values.port) || 587,
        smtpUsername: values.username,
        smtpPassword: values.password,
        fromEmail: values.fromEmail,
        fromName: values.fromName || ProjectDisplayName,
      });
      void message.success(t('pages.systemInitializePage.complete.message'));
      setCurrentStep(2);
    } catch (error) {
      void message.error(t('pages.systemInitializePage.complete.messages.failed'));
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = () => {
    navigate('/auth/login');
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <Card className="max-w-xl mx-auto shadow-lg rounded-2xl">
            <div className="text-center mb-8">
              <Title level={4} className="m-0">{t('pages.systemInitializePage.adminAccount.title')}</Title>
              <Text type="secondary">{t('pages.systemInitializePage.adminAccount.subtitle')}</Text>
            </div>

            <Form
              form={adminForm}
              layout="vertical"
              onFinish={handleAdminSubmit}
              size="large"
              requiredMark={false}
              autoComplete="off"
            >
              <Form.Item
                name="username"
                rules={[
                  { required: true, message: t('pages.systemInitializePage.adminAccount.form.username.required') },
                  { pattern: /^[a-zA-Z0-9_]+$/, message: t('pages.systemInitializePage.adminAccount.form.username.pattern') },
                  { max: 64, message: t('pages.systemInitializePage.adminAccount.form.username.max') },
                ]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.adminAccount.form.username.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Form.Item
                name="email"
                rules={[
                  { required: true, message: t('pages.systemInitializePage.adminAccount.form.email.required') },
                  { type: 'email', message: t('pages.systemInitializePage.adminAccount.form.email.type') },
                  { max: 256, message: t('pages.systemInitializePage.adminAccount.form.email.max') },
                ]}
              >
                <Input
                  prefix={<MailOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.adminAccount.form.email.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Form.Item
                name="password"
                rules={[
                  { required: true, message: t('pages.systemInitializePage.adminAccount.form.password.required') },
                  { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: t('pages.systemInitializePage.adminAccount.form.password.pattern') },
                  { max: 128, message: t('pages.systemInitializePage.adminAccount.form.password.max') },
                ]}
              >
                <Password
                  prefix={<LockOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.adminAccount.form.password.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Form.Item
                name="confirmPassword"
                dependencies={['password']}
                rules={[
                  { required: true, message: t('pages.systemInitializePage.adminAccount.form.confirmPassword.required') },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('password') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error(t('pages.systemInitializePage.adminAccount.form.confirmPassword.mismatch')));
                    },
                  }),
                ]}
              >
                <Password
                  prefix={<LockOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.adminAccount.form.confirmPassword.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Form.Item className="mb-0">
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none"
                >
                  {t('pages.systemInitializePage.steps.mailServer')}
                </Button>
              </Form.Item>
            </Form>
          </Card>
        );

      case 1:
        return (
          <Card className="max-w-xl mx-auto shadow-lg rounded-2xl">
            <div className="text-center mb-8">
              <Title level={4} className="m-0">{t('pages.systemInitializePage.mailServer.title')}</Title>
              <Text type="secondary">{t('pages.systemInitializePage.mailServer.subtitle')}</Text>
            </div>

            <Form
              form={mailForm}
              layout="vertical"
              onFinish={handleMailSubmit}
              size="large"
              requiredMark={false}
              autoComplete="off"
            >
              <Row gutter={16}>
                <Col xs={24} sm={16}>
                  <Form.Item
                    name="host"
                    label={t('pages.systemInitializePage.mailServer.form.host.label')}
                    rules={[{ required: true, message: t('pages.systemInitializePage.mailServer.form.host.required') }]}
                  >
                    <Input
                      prefix={<MailOutlined className="text-gray-400 mr-2" />}
                      placeholder={t('pages.systemInitializePage.mailServer.form.host.placeholder')}
                      className="rounded-xl h-11"
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} sm={8}>
                  <Form.Item
                    name="port"
                    label={t('pages.systemInitializePage.mailServer.form.port.label')}
                    initialValue={587}
                  >
                    <Input type="number" placeholder={t('pages.systemInitializePage.mailServer.form.port.placeholder')} className="rounded-xl h-11" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="username"
                label={t('pages.systemInitializePage.mailServer.form.username.label')}
                rules={[{ required: true, message: t('pages.systemInitializePage.mailServer.form.username.required') }]}
              >
                <Input
                  prefix={<UserOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.mailServer.form.username.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Form.Item
                name="password"
                label={t('pages.systemInitializePage.mailServer.form.password.label')}
                rules={[{ required: true, message: t('pages.systemInitializePage.mailServer.form.password.required') }]}
              >
                <Password
                  prefix={<LockOutlined className="text-gray-400 mr-2" />}
                  placeholder={t('pages.systemInitializePage.mailServer.form.password.placeholder')}
                  className="rounded-xl h-11"
                />
              </Form.Item>

              <Row gutter={16}>
                <Col xs={24} sm={12}>
                  <Form.Item
                    name="fromEmail"
                    label={t('pages.systemInitializePage.mailServer.form.fromEmail.label')}
                    rules={[
                      { required: true, message: t('pages.systemInitializePage.mailServer.form.fromEmail.required') },
                      { type: 'email', message: t('pages.systemInitializePage.mailServer.form.fromEmail.type') },
                    ]}
                  >
                    <Input
                      prefix={<MailOutlined className="text-gray-400 mr-2" />}
                      placeholder={t('pages.systemInitializePage.mailServer.form.fromEmail.placeholder')}
                      className="rounded-xl h-11"
                    />
                  </Form.Item>
                </Col>
                <Col xs={24} sm={12}>
                  <Form.Item
                    name="fromName"
                    label={t('pages.systemInitializePage.mailServer.form.fromName.label')}
                    initialValue={ProjectDisplayName}
                    rules={[{ required: true, message: t('pages.systemInitializePage.mailServer.form.fromName.required') }]}
                  >
                    <Input placeholder={ProjectDisplayName} className="rounded-xl h-11" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item className="mb-0">
                <div className="flex gap-4">
                  <Button
                    onClick={() => setCurrentStep(0)}
                    className="flex-1 h-12 text-base font-semibold rounded-xl"
                  >
                    {t('pages.systemInitializePage.steps.adminAccount')}
                  </Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={loading}
                    className="flex-1 h-12 text-base font-semibold shadow-lg rounded-xl border-none"
                  >
                    {t('pages.systemInitializePage.steps.complete')}
                  </Button>
                </div>
              </Form.Item>
            </Form>
          </Card>
        );

      case 2:
        return (
          <Card className="max-w-xl mx-auto shadow-lg rounded-2xl">
            <div className="text-center py-8">
              <CheckCircleOutlined className="text-6xl text-green-500 mb-6" />
              <Title level={3} className="m-0 mb-2">{t('pages.systemInitializePage.complete.title')}</Title>
              <Text type="secondary" className="block mb-8">
                {t('pages.systemInitializePage.complete.subtitle')}
              </Text>

              <Button
                type="primary"
                size="large"
                loading={loading}
                onClick={handleComplete}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none"
              >
                {t('pages.systemInitializePage.complete.button')}
              </Button>
            </div>
          </Card>
        );

      default:
        return null;
    }
  };

  if (checkingStatus) {
    return (
      <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-gray-50 via-white to-gray-50">
        <Spin size="large" tip={t('pages.systemInitializePage.loading')} />
      </div>
    );
  }

  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-gray-50 via-white to-gray-50 relative overflow-hidden p-6">
      <div className="absolute top-6 left-1/2 -translate-x-1/2 flex items-center gap-2">
        <img src="/logo.svg" alt="Logo" className="w-10 h-10" />
        <span className="text-2xl font-bold tracking-tight text-gray-900">
          {ProjectDisplayName}
        </span>
      </div>

      <div className="absolute top-6 right-6">
        <LanguageSwitcher />
      </div>

      <div className="w-full max-w-3xl mt-16">
        <Steps
          current={currentStep}
          items={steps}
          className="mb-8"
          size="default"
        />

        {renderStepContent()}
      </div>
    </div>
  );
}
