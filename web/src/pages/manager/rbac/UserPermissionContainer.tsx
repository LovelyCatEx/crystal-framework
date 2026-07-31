import {Segmented} from "antd";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import OverviewPage from "./UserPermissionOverviewPage.tsx";
import ManagementPage from "./UserPermissionManagerPage.tsx";

type ViewMode = "overview" | "management";

export default function UserPermissionContainer() {
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
                        {value: "overview", label: t("pages.userPermissionManager.switch.overview")},
                        {value: "management", label: t("pages.userPermissionManager.switch.management")},
                    ]}
                />
            </div>
            {viewMode === "overview" && <OverviewPage/>}
            {viewMode === "management" && <ManagementPage/>}
        </>
    );
}
