import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import {Button, Col, Form, Input, message, Row} from 'antd';
import {LockOutlined, MailOutlined} from '@ant-design/icons';
import {AuthCardLayout} from './AuthorizationPage.tsx';
import {requestPasswordResetEmailCode, resetPassword, type ResetPasswordDTO} from "@/api/auth.api.ts";
import {buildDocumentTitle} from "@/global/global-settings.ts";

const { Password } = Input;

export function ForgotPasswordPage() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = buildDocumentTitle(t('pages.auth.forgotPassword.title'))
  }, [t]);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => {
        setCountdown(countdown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const handleSendCode = async () => {
    const email = form.getFieldValue('email');
    if (!email) {
      void message.warning(t('pages.auth.forgotPassword.messages.emailRequired'));
      return;
    }

    setSendingCode(true);
    try {
      await requestPasswordResetEmailCode(email);
      void message.success(t('pages.auth.forgotPassword.messages.codeSendSuccess'));
      setCountdown(60);
    } catch (error) {
      void message.error(t('pages.auth.forgotPassword.messages.codeSendFailed'));
    } finally {
      setSendingCode(false);
    }
  };

  const onFinish = async (values: ResetPasswordDTO) => {
    setLoading(true);
    try {
      await resetPassword(values);
      void message.success(t('pages.auth.forgotPassword.messages.resetSuccess'));
      navigate('/auth/login');
    } catch (error) {
      void message.error(t('pages.auth.forgotPassword.messages.resetFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthCardLayout
      title={t('pages.auth.forgotPassword.title')}
      subtitle={t('pages.auth.forgotPassword.subtitle')}
      footerText={t('pages.auth.forgotPassword.footerText')}
      footerLink={t('pages.auth.forgotPassword.footerLink')}
      footerAction={() => navigate('/auth/login')}
    >
      <Form
        form={form}
        name="forgot_password_form"
        layout="vertical"
        onFinish={onFinish}
        size="large"
        requiredMark={false}
        autoComplete="off"
      >
        <Form.Item
          name="email"
          rules={[
            { required: true, message: t('pages.auth.forgotPassword.form.email.required') },
            { type: 'email', message: t('pages.auth.forgotPassword.form.email.type') },
            { max: 256, message: t('pages.auth.forgotPassword.form.email.max') }
          ]}
        >
          <Input
            prefix={<MailOutlined className="text-gray-400 mr-2" />}
            placeholder={t('pages.auth.forgotPassword.form.email.placeholder')}
            className="rounded-xl"
          />
        </Form.Item>

        <Form.Item
          name="emailCode"
          rules={[{ required: true, message: t('pages.auth.forgotPassword.form.emailCode.required') }]}
        >
          <Row gutter={12}>
            <Col span={16}>
              <Input
                placeholder={t('pages.auth.forgotPassword.form.emailCode.placeholder')}
                className="rounded-xl"
              />
            </Col>
            <Col span={8}>
              <Button
                type="primary"
                htmlType="button"
                loading={sendingCode}
                disabled={countdown > 0}
                onClick={handleSendCode}
                className="w-full h-10 text-base font-semibold rounded-xl border-none active:scale-[0.98] transition-all"
              >
                {countdown > 0 ? t('pages.auth.forgotPassword.form.emailCode.retry', { count: countdown }) : t('pages.auth.forgotPassword.form.emailCode.send')}
              </Button>
            </Col>
          </Row>
        </Form.Item>

        <Form.Item
          name="newPassword"
          rules={[
            { required: true, message: t('pages.auth.forgotPassword.form.newPassword.required') },
            { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: t('pages.auth.forgotPassword.form.newPassword.pattern') },
            { max: 128, message: t('pages.auth.forgotPassword.form.newPassword.max') }
          ]}
        >
          <Password
            prefix={<LockOutlined className="text-gray-400 mr-2" />}
            placeholder={t('pages.auth.forgotPassword.form.newPassword.placeholder')}
            className="rounded-xl"
          />
        </Form.Item>

        <Form.Item
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            { required: true, message: t('pages.auth.forgotPassword.form.confirmPassword.required') },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error(t('pages.auth.forgotPassword.form.confirmPassword.mismatch')));
              },
            }),
          ]}
        >
          <Password
            prefix={<LockOutlined className="text-gray-400 mr-2" />}
            placeholder={t('pages.auth.forgotPassword.form.confirmPassword.placeholder')}
            className="rounded-xl"
          />
        </Form.Item>

        <Form.Item className="mb-4">
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
          >
            {t('pages.auth.forgotPassword.form.submit')}
          </Button>
        </Form.Item>
      </Form>
    </AuthCardLayout>
  );
}
