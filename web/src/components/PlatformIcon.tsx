import React from "react";
import {GithubOutlined, GoogleOutlined, QqOutlined} from "@ant-design/icons";
import { Space, Tag } from "antd";
import { OAuthPlatform } from "../types/oauth-account.types";

interface PlatformIconProps {
    platform: OAuthPlatform | string;
    showText?: boolean;
    className?: string;
}

const PLATFORM_CONFIG: Record<OAuthPlatform, { icon: React.ReactNode; label: string }> = {
    [OAuthPlatform.GITHUB]: {
        icon: <GithubOutlined />,
        label: "GitHub"
    },
    [OAuthPlatform.GOOGLE]: {
        icon: <GoogleOutlined />,
        label: "Google"
    },
    [OAuthPlatform.OICQ]: {
        icon: <QqOutlined />,
        label: "QQ"
    }
};

export const PlatformIcon: React.FC<PlatformIconProps> = ({ 
    platform, 
    showText = true,
    className = ""
}) => {
    const config = PLATFORM_CONFIG[platform as OAuthPlatform] || {
        icon: null,
        label: platform
    };

    return (
        <Space className={`text-gray-500 text-sm flex items-center gap-2 ${className}`}>
            {config.icon && (
                <span>
                    {config.icon}
                </span>
            )}
            {showText && <span>{config.label}</span>}
        </Space>
    );
};

export const PlatformTag: React.FC<PlatformIconProps> = ({ 
    platform,
    className = ""
}) => {
    const config = PLATFORM_CONFIG[platform as OAuthPlatform] || {
        icon: null,
        color: "#999",
        label: platform
    };

    return (
        <Tag 
            className={`m-0 flex items-center gap-1 ${className}`}
        >
            {config.icon}
            <span>{config.label}</span>
        </Tag>
    );
};

export default PlatformIcon;
