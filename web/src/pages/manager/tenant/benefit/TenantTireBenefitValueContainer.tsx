import {Segmented} from "antd";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import OverviewPage from "./TenantTireBenefitValueOverviewPage.tsx";
import ManagementPage from "./TenantTireBenefitValueManagementPage.tsx";

export default function BenefitValueContainer() {
    const {t} = useTranslation();
    const [viewMode, setViewMode] = useState<'overview' | 'management'>('overview');

    return (
        <>
            <div className="flex justify-center">
                <Segmented
                    value={viewMode}
                    onChange={(value) => setViewMode(value as 'overview' | 'management')}
                    options={[
                        { value: 'overview', label: t('pages.tenantTireBenefitValueManager.switch.overview') },
                        { value: 'management', label: t('pages.tenantTireBenefitValueManager.switch.management') },
                    ]}
                />
            </div>
            {viewMode === 'overview' ? <OverviewPage /> : <ManagementPage />}
        </>
    );
}
