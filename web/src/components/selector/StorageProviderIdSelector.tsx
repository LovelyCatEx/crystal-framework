import {CloudOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useStorageProviderTableColumns} from "../columns/StorageProviderEntityColumns.tsx";
import {StorageProviderManagerController} from "@/api/resource/storage-provider.api.ts";
import type {StorageProvider} from "@/types/resource/storage-provider.types.ts";
import {useStorageProviderTypes} from "@/compositions/use-storage-provider-types.ts";

interface StorageProviderIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
}

export function StorageProviderIdSelector({ value, onChange }: StorageProviderIdSelectorProps) {
    const { t } = useTranslation();
    const columns = useStorageProviderTableColumns();
    const { getLabel } = useStorageProviderTypes();

    return (
        <EntityIdSelector<StorageProvider>
            value={value}
            onChange={onChange}
            isRowDisabled={(row) => !row.active}
            entityName={t('entityNames.storageProvider')}
            columns={columns}
            controller={StorageProviderManagerController}
            displayRender={(provider) => `${provider.name} (${getLabel(provider.type)})`}
            placeholder={t('components.selector.entityIdSelector.placeholder')}
            icon={<CloudOutlined />}
        />
    );
}
