import { Tag, message } from "antd";
import { CopyOutlined } from "@ant-design/icons";
import type { MailTemplateType } from "../types/mail.types.ts";
import { useTranslation } from "react-i18next";

interface TemplateVariablesTagProps {
    templateType: MailTemplateType | null;
}

export function TemplateVariablesTag({ templateType }: TemplateVariablesTagProps) {
    const { t } = useTranslation();

    if (!templateType) {
        return null;
    }

    let variables: string[] = [];
    try {
        const parsed = JSON.parse(templateType.variables);
        if (Array.isArray(parsed)) {
            variables = parsed;
        } else if (typeof parsed === "object" && parsed !== null) {
            variables = Object.keys(parsed);
        }
    } catch {
        return null;
    }

    if (variables.length === 0) {
        return null;
    }

    const handleCopy = (variable: string) => {
        navigator.clipboard.writeText(`{{${variable}}}`).then(() => {
            void message.success(t('components.templateVariablesTag.copySuccess', { variable: `{{${variable}}}` }));
        });
    };

    return (
        <div className="flex flex-wrap gap-2 mt-2">
            {variables.map((variable) => (
                <Tag
                    key={variable}
                    color="blue"
                    className="cursor-pointer hover:opacity-80"
                    icon={<CopyOutlined />}
                    onClick={() => handleCopy(variable)}
                >
                    {variable}
                </Tag>
            ))}
        </div>
    );
}
