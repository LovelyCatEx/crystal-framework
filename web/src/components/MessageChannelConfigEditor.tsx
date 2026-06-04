import {Button, Select, Space, Tooltip} from "antd";
import {JsonEditor} from "./JsonEditor.tsx";
import {useState} from "react";
import {MailOutlined, MessageOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ChannelType} from "@/types/tenant/tenant-message-channel.types.ts";

interface MessageChannelConfigEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
}

interface ChannelConfigTemplate {
    label: string;
    key: ChannelType;
    icon: React.ReactNode;
    config: Record<string, unknown>;
}

export function MessageChannelConfigEditor({value, onChange, placeholder}: MessageChannelConfigEditorProps) {
    const {t} = useTranslation();
    const [selectedTemplate, setSelectedTemplate] = useState<ChannelType>();

    const templates: ChannelConfigTemplate[] = [
        {
            label: t('components.messageChannelConfig.email'),
            key: ChannelType.EMAIL,
            icon: <MailOutlined/>,
            config: {
                host: "",
                port: 465,
                username: "",
                password: "",
                ssl: true,
                fromEmail: ""
            }
        },
        {
            label: t('components.messageChannelConfig.lark'),
            key: ChannelType.LARK,
            icon: <MessageOutlined/>,
            config: {
                appId: "",
                appSecret: "",
                baseUrl: "https://open.feishu.cn"
            }
        }
    ];

    const handleApplyTemplate = () => {
        const template = templates.find(it => it.key === selectedTemplate);
        if (template) {
            onChange?.(JSON.stringify(template.config, null, 2));
        }
    };

    return (
        <Space orientation="vertical" className="w-full" size="middle">
            <Space className="w-full">
                <Select
                    placeholder={t('components.messageChannelConfig.selectTemplate')}
                    value={selectedTemplate}
                    onChange={setSelectedTemplate}
                    options={templates.map(it => ({
                        label: (
                            <Space>
                                {it.icon}
                                {it.label}
                            </Space>
                        ),
                        value: it.key
                    }))}
                    className="min-w-48"
                />
                <Tooltip title={t('components.messageChannelConfig.applyTemplateTooltip')}>
                    <Button
                        type="primary"
                        onClick={handleApplyTemplate}
                        disabled={selectedTemplate === undefined}
                    >
                        {t('components.messageChannelConfig.applyTemplate')}
                    </Button>
                </Tooltip>
            </Space>
            <JsonEditor
                value={value}
                onChange={onChange}
                placeholder={placeholder}
            />
        </Space>
    );
}
