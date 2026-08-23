import {useSWRComposition} from "@/compositions/use-swr.ts";
import {getStorageProviderTypes} from "@/api/resource/storage-provider.api.ts";
import type {StorageProviderTypeDeclaration} from "@/types/resource/storage-provider.types.ts";
import i18n from "@/i18n";

export const SWR_KEY_STORAGE_PROVIDER_TYPES = 'storageProviderTypes';

export function useStorageProviderTypes() {
    const {data, isLoading} = useSWRComposition<StorageProviderTypeDeclaration[]>(
        SWR_KEY_STORAGE_PROVIDER_TYPES,
        () => getStorageProviderTypes(),
    );

    const types = data ?? [];

    function getDeclaration(typeId: number): StorageProviderTypeDeclaration | undefined {
        return types.find(t => t.typeId === typeId);
    }

    function getLabel(typeId: number): string {
        const i18nKey = `enums.storageProviderType.${typeId}`;
        if (i18n.exists(i18nKey)) {
            return i18n.t(i18nKey);
        }
        const decl = getDeclaration(typeId);
        if (decl) {
            return decl.displayName;
        }
        const fallback = i18n.t('enums.unknown');
        return fallback === 'enums.unknown' ? String(typeId) : `${fallback} (${typeId})`;
    }

    return {types, isLoading, getDeclaration, getLabel};
}
