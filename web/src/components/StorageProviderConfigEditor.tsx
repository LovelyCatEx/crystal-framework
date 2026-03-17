import { Button, Select, Space, Tooltip } from "antd";
import { JsonEditor } from "./JsonEditor.tsx";
import { useState } from "react";
import {FileImageOutlined, CloudOutlined, FolderOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

interface StorageProviderConfigEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
}

export function StorageProviderConfigEditor({ value, onChange, placeholder }: StorageProviderConfigEditorProps) {
    const {t} = useTranslation();
    const [selectedTemplate, setSelectedTemplate] = useState<string>();

    const templates = [
        {
            label: t('components.storageProviderConfig.localFileSystem'),
            key: "local_file_system",
            icon: <FolderOutlined />,
            config: {
                basePath: "",
            }
        },
        {
            label: t('components.storageProviderConfig.tencentCos'),
            key: "tencent_cos",
            icon: <CloudOutlined />,
            config: {
                accessKey: "",
                secretKey: "",
                region: "",
                bucketName: ""
            }
        },
        {
            label: t('components.storageProviderConfig.aliyunOss'),
            key: "aliyun_oss",
            icon: <FileImageOutlined />,
            config: {
                accessKeyId: "",
                accessKeySecret: "",
                securityToken: "",
                region: "",
                bucketName: ""
            }
        }
    ];

    const handleApplyTemplate = () => {
        const template = templates.find(t => t.key === selectedTemplate);
        if (template) {
            onChange?.(JSON.stringify(template.config, null, 2));
        }
    };

    return (
        <Space direction="vertical" className="w-full" size="middle">
            <Space className="w-full">
                <Select
                    placeholder={t('components.storageProviderConfig.selectTemplate')}
                    value={selectedTemplate}
                    onChange={setSelectedTemplate}
                    options={templates.map(t => ({
                        label: (
                            <Space>
                                {t.icon}
                                {t.label}
                            </Space>
                        ),
                        value: t.key
                    }))}
                    className="min-w-48"
                />
                <Tooltip title={t('components.storageProviderConfig.applyTemplateTooltip')}>
                    <Button 
                        type="primary" 
                        onClick={handleApplyTemplate}
                        disabled={!selectedTemplate}
                    >
                        {t('components.storageProviderConfig.applyTemplate')}
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
