import {Card, Descriptions, Spin, Tag} from "antd";
import type {MailTemplateType} from "../../../types/mail.types.ts";
import {useSWRComposition} from "../../../compositions/swr.ts";
import {MailTemplateTypeManagerController} from "../../../api/mail-template-type.api.ts";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";

interface MailTemplateTypeCardProps {
    typeId: string;
}

export function MailTemplateTypeCard({ typeId }: MailTemplateTypeCardProps) {
    const { data: templateType, isLoading } = useSWRComposition<MailTemplateType | null>(
        `mail-template-type-${typeId}`,
        async () => {
            return await MailTemplateTypeManagerController.getById(typeId);
        }
    );

    if (isLoading) {
        return (
            <Card size="small" className="w-64">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!templateType) {
        return (
            <Card size="small" className="w-64">
                <div className="text-center py-4 text-gray-400">
                    未找到类型信息
                </div>
            </Card>
        );
    }

    return (
        <Card 
            size="small" 
            className="w-72"
            title={
                <div className="flex items-center gap-2">
                    <CopyableToolTip title={templateType.name}>
                        <span className="font-bold">{templateType.name}</span>
                    </CopyableToolTip>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={templateType.id}>
                        <Tag color="blue" className="text-xs">{templateType.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label="描述">
                    <span className="text-gray-600">
                        {templateType.description ?? "-"}
                    </span>
                </Descriptions.Item>
                <Descriptions.Item label="允许多模板">
                    <Tag color={templateType.allowMultiple ? "green" : "orange"} className="text-xs">
                        {templateType.allowMultiple ? "是" : "否"}
                    </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="变量">
                    <div className="text-xs font-mono text-gray-500 flex flex-row flex-wrap gap-2 items-start">
                        {(JSON.parse(templateType.variables) as string[]).map((variable) => (
                            <CopyableToolTip title={variable} key={variable}>
                                <Tag>{variable}</Tag>
                            </CopyableToolTip>
                        ))}
                    </div>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
