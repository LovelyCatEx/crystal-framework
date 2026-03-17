import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import {Button, Checkbox, Col, Form, Input, message, Row} from 'antd';
import {LockOutlined, MailOutlined, UserOutlined} from '@ant-design/icons';
import {AuthCardLayout} from './AuthorizationPage.tsx';
import {register, requestRegisterEmailCode} from '@/api/auth.api.ts';
import {buildDocumentTitle} from "@/global/global-settings.ts";
import {menuPathLogin} from "@/router";

const { Password } = Input;

interface RegisterFormData {
  username: string;
  password: string;
  email: string;
  emailCode: string;
}

export function RegisterPage() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = buildDocumentTitle(t('pages.auth.register.title'))
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
      void message.warning(t('pages.auth.register.messages.emailRequired'));
      return;
    }

    setSendingCode(true);
    try {
      await requestRegisterEmailCode(email);
      void message.success(t('pages.auth.register.messages.codeSendSuccess'));
      setCountdown(60);
    } catch (error) {
      void message.error(t('pages.auth.register.messages.codeSendFailed'));
    } finally {
      setSendingCode(false);
    }
  };

  const onFinish = async (values: RegisterFormData) => {
    setLoading(true);
    try {
      await register(values.username, values.password, values.email, values.emailCode);
      void message.success(t('pages.auth.register.messages.registerSuccess'));
      navigate(menuPathLogin);
    } catch (error) {
      void message.error(t('pages.auth.register.messages.registerFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
      <AuthCardLayout
          title={t('pages.auth.register.title')}
          subtitle={t('pages.auth.register.subtitle')}
          footerText={t('pages.auth.register.footerText')}
          footerLink={t('pages.auth.register.footerLink')}
          footerAction={() => navigate(menuPathLogin)}
      >
        <Form
            form={form}
            name="register_form"
            layout="vertical"
            onFinish={onFinish}
            size="large"
            requiredMark={false}
            autoComplete="off"
        >
          <Form.Item
              name="username"
              rules={[
                { required: true, message: t('pages.auth.register.form.username.required') },
                { pattern: /^[a-zA-Z0-9_]+$/, message: t('pages.auth.register.form.username.pattern') },
                { max: 64, message: t('pages.auth.register.form.username.max') }
              ]}
          >
            <Input
                prefix={<UserOutlined className="text-gray-400 mr-2" />}
                placeholder={t('pages.auth.register.form.username.placeholder')}
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="email"
              rules={[
                { required: true, message: t('pages.auth.register.form.email.required') },
                { type: 'email', message: t('pages.auth.register.form.email.type') },
                { max: 256, message: t('pages.auth.register.form.email.max') }
              ]}
          >
            <Input
                prefix={<MailOutlined className="text-gray-400 mr-2" />}
                placeholder={t('pages.auth.register.form.email.placeholder')}
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="emailCode"
              rules={[{ required: true, message: t('pages.auth.register.form.emailCode.required') }]}
          >
            <Row gutter={12}>
              <Col span={16}>
                <Input
                    placeholder={t('pages.auth.register.form.emailCode.placeholder')}
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
                  {countdown > 0 ? t('pages.auth.register.form.emailCode.retry', { count: countdown }) : t('pages.auth.register.form.emailCode.send')}
                </Button>
              </Col>
            </Row>
          </Form.Item>

          <Form.Item
              name="password"
              rules={[
                { required: true, message: t('pages.auth.register.form.password.required') },
                { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: t('pages.auth.register.form.password.pattern') },
                { max: 128, message: t('pages.auth.register.form.password.max') }
              ]}
          >
            <Password
                prefix={<LockOutlined className="text-gray-400 mr-2" />}
                placeholder={t('pages.auth.register.form.password.placeholder')}
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="confirmPassword"
              dependencies={['password']}
              rules={[
                { required: true, message: t('pages.auth.register.form.confirmPassword.required') },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error(t('pages.auth.register.form.confirmPassword.mismatch')));
                  },
                }),
              ]}
          >
            <Password
                prefix={<LockOutlined className="text-gray-400 mr-2" />}
                placeholder={t('pages.auth.register.form.confirmPassword.placeholder')}
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="agreement"
              valuePropName="checked"
              rules={[{ required: true, message: t('pages.auth.register.form.agreement.required') }]}
              className="mb-6"
          >
            <Checkbox className="text-xs text-gray-500">
              {t('pages.auth.register.form.agreement.text')}
              <a href="/privacy" className="text-pink-400 hover:underline mx-1" target="_blank">{t('pages.auth.register.form.agreement.privacyPolicy')}</a>
              {t('pages.auth.register.form.agreement.and')}
              <a href="/terms" className="text-pink-400 hover:underline mx-1" target="_blank">{t('pages.auth.register.form.agreement.termsOfService')}</a>
            </Checkbox>
          </Form.Item>

          <Form.Item className="mb-4">
            <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
            >
              {t('pages.auth.register.form.submit')}
            </Button>
          </Form.Item>
        </Form>
      </AuthCardLayout>
  );
}
