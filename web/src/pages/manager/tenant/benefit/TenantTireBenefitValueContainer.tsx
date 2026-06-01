import {Segmented} from "antd";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import OverviewPage from "./TenantTireBenefitValueOverviewPage.tsx";
import CrossOverviewPage from "./TenantTireBenefitValueCrossOverviewPage.tsx";
import ManagementPage from "./TenantTireBenefitValueManagementPage.tsx";

export default function BenefitValueContainer() {
    const {t} = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const viewMode = (searchParams.get('tab') as 'plan-benefits' | 'cross-overview' | 'management') || 'cross-overview';

    return (
        <>
            <div className="flex justify-center">
                <Segmented
                    value={viewMode}
                    onChange={(value) => {
                        const params = new URLSearchParams(searchParams.toString());
                        params.set('tab', value as string);
                        setSearchParams(params, { replace: true });
                    }}
                    options={[
                        { value: 'cross-overview', label: t('pages.tenantTireBenefitValueManager.switch.crossOverview') },
                        { value: 'plan-benefits', label: t('pages.tenantTireBenefitValueManager.switch.planBenefits') },
                        { value: 'management', label: t('pages.tenantTireBenefitValueManager.switch.management') },
                    ]}
                />
            </div>
            {viewMode === 'plan-benefits' && <OverviewPage />}
            {viewMode === 'cross-overview' && <CrossOverviewPage />}
            {viewMode === 'management' && <ManagementPage />}
        </>
    );
}
