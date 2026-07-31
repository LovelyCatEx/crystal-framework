import {Space, Switch} from "antd";
import {useCallback, useState} from "react";
import {useTranslation} from "react-i18next";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {usePermissionTranslator} from "@/i18n/permission-translations.tsx";
import {getPermissionType} from "@/i18n/enum-helpers.ts";
import type {UserPermission} from "@/types/user/rbac/user-permission.types.ts";
import type {
    ManagerCreatePermissionDTO,
    ManagerReadPermissionDTO,
    ManagerUpdatePermissionDTO,
} from "@/api/user/rbac/user-permission.api.ts";

const SWR_KEY = "manager.user-permission.tree";

export default function UserPermissionOverviewPage() {
    const {t} = useTranslation();
    const {controller} = useProtectedController<
        UserPermission,
        ManagerCreatePermissionDTO,
        ManagerReadPermissionDTO,
        ManagerUpdatePermissionDTO
    >();
    const translatePermission = usePermissionTranslator();
    const [useI18nDescription, setUseI18nDescription] = useState(true);

    const [permissions, , loading] = useSWRState<UserPermission[]>(
        SWR_KEY,
        () => controller.list(),
    );

    const descriptionRender = useCallback((row: UserPermission) => {
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
                typeLabel={getPermissionType}
                descriptionRender={descriptionRender}
            />
        </StandardCard>
    );
}
