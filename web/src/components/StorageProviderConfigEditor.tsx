import { Button, Select, Space, Tooltip } from "antd";
import { JsonEditor } from "./JsonEditor.tsx";
import { useState } from "react";
import {FileImageOutlined, CloudOutlined, FolderOutlined} from "@ant-design/icons";

interface StorageProviderConfigEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
}

const templates = [
    {
        label: "本地文件系统",
        key: "local_file_system",
        icon: <FolderOutlined />,
        config: {
            basePath: "",
        }
    },
    {
        label: "腾讯云 COS",
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
        label: "阿里云 OSS",
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

export function StorageProviderConfigEditor({ value, onChange, placeholder }: StorageProviderConfigEditorProps) {
    const [selectedTemplate, setSelectedTemplate] = useState<string>();

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
                    placeholder="选择配置模板"
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
                <Tooltip title="应用模板">
                    <Button 
                        type="primary" 
                        onClick={handleApplyTemplate}
                        disabled={!selectedTemplate}
                    >
                        应用模板
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
