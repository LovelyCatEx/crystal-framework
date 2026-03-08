import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
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
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = buildDocumentTitle('注册')
  }, []);

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
      void message.warning('请先输入邮箱');
      return;
    }

    setSendingCode(true);
    try {
      await requestRegisterEmailCode(email);
      void message.success('验证码发送成功，请注意查收');
      setCountdown(60);
    } catch (error) {
      void message.error('验证码发送失败，请稍后重试');
      console.error('发送验证码失败:', error);
    } finally {
      setSendingCode(false);
    }
  };

  const onFinish = async (values: RegisterFormData) => {
    setLoading(true);
    try {
      await register(values.username, values.password, values.email, values.emailCode);
      void message.success('注册成功！');
      navigate(menuPathLogin);
    } catch (error) {
      void message.error('注册失败，请稍后重试');
      console.error('注册失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
      <AuthCardLayout
          title="创建账号"
          subtitle="开启您的全新旅程"
          footerText="已经有账号了?"
          footerLink="返回登录"
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
                { required: true, message: '请输入用户名' },
                { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含数字、字母和下划线' },
                { max: 64, message: '用户名长度不能超过64个字符' }
              ]}
          >
            <Input
                prefix={<UserOutlined className="text-gray-400 mr-2" />}
                placeholder="用户名"
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '邮箱格式不正确' },
                { max: 256, message: '邮箱长度不能超过256个字符' }
              ]}
          >
            <Input
                prefix={<MailOutlined className="text-gray-400 mr-2" />}
                placeholder="电子邮箱"
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="emailCode"
              rules={[{ required: true, message: '请输入验证码' }]}
          >
            <Row gutter={12}>
              <Col span={16}>
                <Input
                    placeholder="验证码"
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
                  {countdown > 0 ? `${countdown}s后重试` : '发送验证码'}
                </Button>
              </Col>
            </Row>
          </Form.Item>

          <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$/, message: '密码至少8位，且包含数字和字母' },
                { max: 128, message: '密码长度不能超过128个字符' }
              ]}
          >
            <Password
                prefix={<LockOutlined className="text-gray-400 mr-2" />}
                placeholder="密码"
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="confirmPassword"
              dependencies={['password']}
              rules={[
                { required: true, message: '请确认密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'));
                  },
                }),
              ]}
          >
            <Password
                prefix={<LockOutlined className="text-gray-400 mr-2" />}
                placeholder="确认密码"
                className="rounded-xl"
            />
          </Form.Item>

          <Form.Item
              name="agreement"
              valuePropName="checked"
              rules={[{ required: true, message: '请阅读并同意服务条款和隐私政策' }]}
              className="mb-6"
          >
            <Checkbox className="text-xs text-gray-500">
              我已阅读并同意
              <a href="/privacy" className="text-pink-400 hover:underline mx-1" target="_blank">隐私政策</a>
              和
              <a href="/terms" className="text-pink-400 hover:underline mx-1" target="_blank">服务条款</a>
            </Checkbox>
          </Form.Item>

          <Form.Item className="mb-4">
            <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
            >
              注册账号
            </Button>
          </Form.Item>
        </Form>
      </AuthCardLayout>
  );
}
