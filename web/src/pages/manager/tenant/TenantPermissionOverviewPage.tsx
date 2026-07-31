import {Space, Switch} from "antd";
import {useCallback, useState} from "react";
import {useTranslation} from "react-i18next";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {usePermissionTranslator} from "@/i18n/permission-translations.tsx";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";
import type {TenantPermission} from "@/types/tenant/rbac/tenant-permission.types.ts";
import type {
    ManagerCreateTenantPermissionDTO,
    ManagerReadTenantPermissionDTO,
    ManagerUpdateTenantPermissionDTO,
} from "@/api/tenant/rbac/tenant-permission.api.ts";

const SWR_KEY = "manager.tenant-permission.tree";

export default function TenantPermissionOverviewPage() {
    const {t} = useTranslation();
    const {controller} = useProtectedController<
        TenantPermission,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO
    >();
    const translatePermission = usePermissionTranslator();
    const [useI18nDescription, setUseI18nDescription] = useState(true);

    const [permissions, , loading] = useSWRState<TenantPermission[]>(
        SWR_KEY,
        () => controller.list(),
    );

    const descriptionRender = useCallback((row: TenantPermission) => {
        if (!useI18nDescription) return row.description || "-";
        return translatePermission(row.name) ?? (row.description || "-");
    }, [useI18nDescription, translatePermission]);

    return (
        <StandardCard>
            <div className="mb-4">
                <Space size={8}>
                    <span>{t("pages.permissionCatalog.source.label")}</span>
                    <Switch
                        checked={useI18nDescription}
                        checkedChildren={t("pages.permissionCatalog.source.i18n")}
                        unCheckedChildren={t("pages.permissionCatalog.source.db")}
                        onChange={setUseI18nDescription}
                    />
                </Space>
            </div>
            <PermissionTreeTable
                permissions={permissions ?? []}
                loading={loading}
                typeLabel={getTenantPermissionType}
                descriptionRender={descriptionRender}
            />
        </StandardCard>
    );
}
