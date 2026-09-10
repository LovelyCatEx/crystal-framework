import {useTranslation} from "react-i18next";
import {EntityIdsSelector} from "./EntityIdsSelector.tsx";
import {useAiModelTableColumns} from "../columns/AiModelEntityColumns.tsx";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import {Tag} from "antd";

interface AiModelIdsSelectorProps {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function AiModelIdsSelector({ value, onChange }: AiModelIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useAiModelTableColumns();

    return (
        <EntityIdsSelector<AiModelEntity>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.aiModel')}
            columns={columns}
            query={async (params) => (await AiModelManagerController.query(params)).data!}
            getById={(id) => AiModelManagerController.getById(id)}
            renderItem={(model) => (
                <Tag color="blue">{model.displayName}</Tag>
            )}
        />
    );
}
