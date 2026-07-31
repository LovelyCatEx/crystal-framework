import {Segmented} from "antd";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import OverviewPage from "./TenantPermissionOverviewPage.tsx";
import ManagementPage from "./TenantPermissionManagerPage.tsx";

type ViewMode = "overview" | "management";

export default function TenantPermissionContainer() {
    const {t} = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const viewMode = (searchParams.get("tab") as ViewMode) || "overview";

    return (
        <>
            <div className="flex justify-center mb-4">
                <Segmented
                    value={viewMode}
                    onChange={(value) => {
                        const params = new URLSearchParams(searchParams.toString());
                        params.set("tab", value as string);
                        setSearchParams(params, {replace: true});
                    }}
                    options={[
                        {value: "overview", label: t("pages.tenantPermissionManager.switch.overview")},
                        {value: "management", label: t("pages.tenantPermissionManager.switch.management")},
                    ]}
                />
            </div>
            {viewMode === "overview" && <OverviewPage/>}
            {viewMode === "management" && <ManagementPage/>}
        </>
    );
}
