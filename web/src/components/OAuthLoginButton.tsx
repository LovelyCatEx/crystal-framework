import React from "react";
import { Button, message } from "antd";
import {GithubOutlined, GoogleOutlined, QqOutlined} from "@ant-design/icons";
import { getOAuth2LoginUrl } from "../utils/oauth2";
import { OAuthPlatform } from "../types/oauth-account.types";

interface OAuthLoginButtonProps {
    platform: OAuthPlatform;
    agreedToTerms: boolean;
    className?: string;
}

const PLATFORM_CONFIG: Record<OAuthPlatform, { icon: React.ReactNode; label: string }> = {
    [OAuthPlatform.GITHUB]: {
        icon: <GithubOutlined className="text-lg" />,
        label: "GitHub"
    },
    [OAuthPlatform.GOOGLE]: {
        icon: <GoogleOutlined className="text-lg" />,
        label: "Google"
    },
    [OAuthPlatform.OICQ]: {
        icon: <QqOutlined className="text-lg" />,
        label: "QQ"
    }
};

const PLATFORM_URL_KEY: Record<OAuthPlatform, string> = {
    [OAuthPlatform.GITHUB]: "github",
    [OAuthPlatform.GOOGLE]: "google",
    [OAuthPlatform.OICQ]: "oicq"
};

export const OAuthLoginButton: React.FC<OAuthLoginButtonProps> = ({
    platform,
    agreedToTerms,
    className = "flex-1 h-12 rounded-xl flex items-center justify-center hover:bg-gray-50 border-gray-200"
}) => {
    const config = PLATFORM_CONFIG[platform];

    const handleClick = () => {
        if (!agreedToTerms) {
            void message.warning("请先阅读并同意服务条款和隐私政策");
            return;
        }
        window.location.href = getOAuth2LoginUrl(PLATFORM_URL_KEY[platform]);
    };

    return (
        <Button className={className} onClick={handleClick}>
            {config.icon}
        </Button>
    );
};

export default OAuthLoginButton;
