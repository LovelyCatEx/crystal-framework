import {Button, Checkbox, Form, Input, message} from 'antd';
import {LockOutlined, MailOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthCardLayout} from './AuthorizationPage.tsx';
import {login} from "../../api/auth.api.ts";
import {getUserAuthentication, setUserAuthentication} from "../../utils/token.utils.ts";
import {getQueryString} from "../../utils/url.utils.ts";
import {buildDocumentTitle} from "../../global/global-settings.ts";
import {menuPathDashboard, menuPathRegister, menuPathResetPassword} from "../../router";

interface LoginFormData {
  username: string,
  password: string,
  remember: boolean,
  agreement: boolean
}

export function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    document.title = buildDocumentTitle('登录')
  }, []);

  useEffect(() => {
    const auth = getUserAuthentication();
    if (auth && !auth.expired) {
      navigate(menuPathDashboard);
    }
  }, [navigate]);

  const onFinish = (values: LoginFormData) => {
    setLoading(true);

    if (values.remember) {
      localStorage.setItem('login_username', values.username);
    }

    login(values.username, values.password)
        .then((res) => {
          void message.success('登录成功');
          if (res.data) {
            setUserAuthentication(res.data.token, res.data.expiresIn);
            const redirectTo = getQueryString('redirectTo') || menuPathDashboard;
            navigate(redirectTo);
          } else {
            void message.error('登录失败 未知错误');
          }
        })
        .catch(() => {
          void message.error('登录失败');
        })
        .finally(() => {
          setLoading(false);
        })
  };

  return (
    <AuthCardLayout
      title="欢迎回来"
      subtitle="请输入您的凭据以访问控制台"
      footerText="还没有账号?"
      footerLink="现在注册"
      footerAction={() => navigate(menuPathRegister)}
    >
      <Form
        name="login_form"
        layout="vertical"
        initialValues={{
          username: localStorage.getItem('login_username') || '',
          remember: true
      }}
        onFinish={onFinish}
        size="large"
        requiredMark={false}
        autoComplete="off"
      >
        <Form.Item
          name="username"
          rules={[
            { required: true, message: '请输入用户名或邮箱' },
            { pattern: /^[a-zA-Z0-9_@.-]+$/, message: '只能包含英文字母、数字、下划线和横线或邮箱地址' },
          ]}
        >
          <Input
            prefix={<MailOutlined className="text-gray-400 mr-2" />}
            placeholder="用户名或邮箱"
            className="rounded-xl"
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[{ required: true, message: '请输入密码' }]}
        >
          <Input.Password
            prefix={<LockOutlined className="text-gray-400 mr-2" />}
            placeholder="密码"
            className="rounded-xl"
          />
        </Form.Item>

        <div className="flex justify-between items-center mb-6">
          <Form.Item name="remember" valuePropName="checked" noStyle>
            <Checkbox className="text-xs text-gray-500">记住我</Checkbox>
          </Form.Item>
          <a
            className="text-xs font-medium text-pink-400 hover:text-pink-400 transition-colors"
            href={menuPathResetPassword}
          >
            忘记密码?
          </a>
        </div>

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
            立即登录
          </Button>
        </Form.Item>
      </Form>
    </AuthCardLayout>
  );
}
