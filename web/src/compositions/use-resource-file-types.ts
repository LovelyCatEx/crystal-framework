import {useSWRComposition} from "@/compositions/use-swr.ts";
import {getResourceFileTypes} from "@/api/resource/file-resource.api.ts";
import type {ResourceFileTypeDeclaration} from "@/types/resource/file-resource.types.ts";
import i18n from "@/i18n";

export const SWR_KEY_RESOURCE_FILE_TYPES = 'resourceFileTypes';

export function useResourceFileTypes() {
    const {data, isLoading} = useSWRComposition<ResourceFileTypeDeclaration[]>(
        SWR_KEY_RESOURCE_FILE_TYPES,
        () => getResourceFileTypes(),
    );

    const types = data ?? [];

    function getDeclaration(typeId: number): ResourceFileTypeDeclaration | undefined {
        return types.find(t => t.typeId === typeId);
    }

    function getLabel(typeId: number): string {
        const i18nKey = `enums.resourceFileType.${typeId}`;
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
