import { Button, Card, Space, Typography } from "antd";
import { EditOutlined, EyeOutlined } from "@ant-design/icons";
import { useEffect, useState } from "react";
import TextArea from "antd/es/input/TextArea";
import { useTranslation } from "react-i18next";

const { Text } = Typography;

interface HtmlEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
    height?: number;
}

export function HtmlEditor({
    value = "",
    onChange,
    placeholder,
    height = 400
}: HtmlEditorProps) {
    const { t } = useTranslation();
    const [mode, setMode] = useState<"code" | "preview">("code");
    const [htmlText, setHtmlText] = useState(value);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setHtmlText(value);
    }, [value]);

    const handleHtmlTextChange = (text: string) => {
        setHtmlText(text);
        setError(null);
        onChange?.(text);
    };

    const renderPreview = () => {
        return (
            <iframe
                srcDoc={htmlText}
                className="w-full border rounded-lg bg-white"
                style={{ height: `${height}px` }}
                sandbox="allow-same-origin"
                title="HTML Preview"
            />
        );
    };

    return (
        <Card
            size="small"
            className="border rounded-lg"
            title={
                <div className="flex justify-between items-center">
                    <Space>
                        <Button
                            type={mode === "code" ? "primary" : "default"}
                            size="small"
                            icon={<EditOutlined />}
                            onClick={() => setMode("code")}
                        >
                            {t('components.htmlEditor.code')}
                        </Button>
                        <Button
                            type={mode === "preview" ? "primary" : "default"}
                            size="small"
                            icon={<EyeOutlined />}
                            onClick={() => setMode("preview")}
                        >
                            {t('components.htmlEditor.preview')}
                        </Button>
                    </Space>
                    {error && <Text type="danger" className="text-xs">{error}</Text>}
                </div>
            }
        >
            {mode === "code" ? (
                <TextArea
                    value={htmlText}
                    onChange={(e) => handleHtmlTextChange(e.target.value)}
                    style={{ height: `${height}px` }}
                    placeholder={placeholder || t('components.htmlEditor.placeholder')}
                    className="font-mono text-xs"
                />
            ) : (
                renderPreview()
            )}
        </Card>
    );
}
