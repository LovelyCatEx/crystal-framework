import {Route, Routes, useNavigate} from 'react-router-dom';
import {LoginPage} from './LoginPage.tsx';
import {RegisterPage} from './RegisterPage.tsx';
import {ForgotPasswordPage} from './ForgotPasswordPage.tsx';
import {GithubOutlined} from '@ant-design/icons';
import {ProjectDisplayName} from "@/global/global-settings.ts";
import {menuPathLogin, menuPathOAuthCode, menuPathRegister, menuPathResetPassword} from "@/router";
import {OAuth2CodePage} from "./OAuth2CodePage.tsx";

export function AuthorizationPage({ parentPath }: { parentPath: string }) {
  const navigate = useNavigate();

  return (
      <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-gray-50 via-white to-gray-50 relative overflow-hidden">
        {/* Top Navigation Bar */}
        <nav className="fixed top-0 left-0 w-full h-16 px-6 sm:px-12 flex items-center justify-between z-50 bg-white/30 backdrop-blur-md border-b border-white/50">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
            <img src="/logo.svg" alt="Logo" className="w-8 h-8"/>
            <span className="text-2xl font-bold tracking-tight text-gray-900">
              {ProjectDisplayName}
            </span>
          </div>

          <div className="flex items-center">
            <a
                href="https://github.com/LovelyCatEx"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 text-gray-600 hover:text-primary hover:bg-white/50 rounded-full transition-all"
            >
              <GithubOutlined className="text-2xl" />
            </a>
          </div>
        </nav>

        {/* Background Decorations */}
        <div className="fixed top-[-10%] left-[-5%] w-72 h-72 bg-primary/20 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
        <div className="fixed bottom-[-10%] right-[-5%] w-96 h-96 bg-cyan-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>

        <Routes>
          <Route path={menuPathLogin.replace(parentPath, "")} element={<LoginPage />} />
          <Route path={menuPathRegister.replace(parentPath, "")} element={<RegisterPage />} />
          <Route path={menuPathResetPassword.replace(parentPath, "")} element={<ForgotPasswordPage />} />
          <Route path={menuPathOAuthCode.replace(parentPath, "")} element={<OAuth2CodePage />} />
        </Routes>
      </div>
  );
}

export function AuthCardLayout({
  children,
  title,
  subtitle,
  footerText,
  footerLink,
  footerAction,
}: {
  children: React.ReactNode;
  title: string;
  subtitle: string;
  footerText?: string;
  footerLink?: string;
  footerAction?: () => void;
}) {
  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 relative overflow-hidden">
      <div className="relative w-full max-w-[440px] bg-white/80 backdrop-blur-xl border border-white shadow-2xl rounded-[2.5rem] overflow-hidden transition-all duration-500 ease-in-out mt-12 z-10">
        <div className="p-8 sm:p-12">
          {/* Title */}
          <div className="text-center mb-10">
            <h1 className="text-3xl font-bold text-gray-800 tracking-tight">
              {title}
            </h1>
            <p className="text-gray-500 mt-2 text-sm">{subtitle}</p>
          </div>

          {children}

          {footerText && footerLink && (
              <div className="mt-10 text-center">
                <span className="text-gray-500 text-sm">{footerText}</span>
                <span
                    onClick={footerAction}
                    className="ml-2 text-sm font-bold text-pink-400 hover:text-pink-400 transition-colors cursor-pointer"
                >
              {footerLink}
            </span>
              </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div className="fixed bottom-6 text-gray-400 text-xs z-0">
        © 2026 {ProjectDisplayName}. All rights reserved.
      </div>
    </div>
  );
}
