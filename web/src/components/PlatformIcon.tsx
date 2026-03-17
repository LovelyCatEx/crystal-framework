import React from "react";
import {GithubOutlined, GoogleOutlined, QqOutlined} from "@ant-design/icons";
import { Space, Tag } from "antd";
import { OAuthPlatform } from "../types/oauth-account.types";
import { getOAuthPlatform } from "@/i18n/enum-helpers.ts";

interface PlatformIconProps {
    platform: OAuthPlatform | string;
    showText?: boolean;
    className?: string;
}

const PLATFORM_CONFIG: Record<OAuthPlatform, { icon: React.ReactNode }> = {
    [OAuthPlatform.GITHUB]: {
        icon: <GithubOutlined />
    },
    [OAuthPlatform.GOOGLE]: {
        icon: <GoogleOutlined />
    },
    [OAuthPlatform.OICQ]: {
        icon: <QqOutlined />
    }
};

export const PlatformIcon: React.FC<PlatformIconProps> = ({ 
    platform, 
    showText = true,
    className = ""
}) => {
    const config = PLATFORM_CONFIG[platform as OAuthPlatform] || {
        icon: null
    };

    return (
        <Space className={`text-gray-500 text-sm flex items-center gap-2 ${className}`}>
            {config.icon && (
                <span>
                    {config.icon}
                </span>
            )}
            {showText && <span>{getOAuthPlatform(platform as OAuthPlatform)}</span>}
        </Space>
    );
};

export const PlatformTag: React.FC<PlatformIconProps> = ({ 
    platform,
    className = ""
}) => {
    const config = PLATFORM_CONFIG[platform as OAuthPlatform] || {
        icon: null
    };

    return (
        <Tag 
            className={`m-0 flex items-center gap-1 ${className}`}
        >
            {config.icon}
            <span>{getOAuthPlatform(platform as OAuthPlatform)}</span>
        </Tag>
    );
};

export default PlatformIcon;
