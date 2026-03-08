import {CloudOutlined} from "@ant-design/icons";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {STORAGE_PROVIDER_MANAGER_TABLE_COLUMNS} from "../columns/StorageProviderEntityColumns.tsx";
import {StorageProviderManagerController} from "@/api/storage-provider.api.ts";
import {type StorageProvider, StorageProviderType} from "@/types/storage-provider.types.ts";

interface StorageProviderIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
}

export function StorageProviderIdSelector({ value, onChange }: StorageProviderIdSelectorProps) {
    return (
        <EntityIdSelector<StorageProvider>
            value={value}
            onChange={onChange}
            isRowDisabled={(row) => !row.active}
            entityName="存储提供商"
            columns={STORAGE_PROVIDER_MANAGER_TABLE_COLUMNS}
            controller={StorageProviderManagerController}
            displayRender={(provider) => `${provider.name} (${StorageProviderType[provider.type]})`}
            placeholder="选择存储提供商"
            icon={<CloudOutlined />}
        />
    );
}
