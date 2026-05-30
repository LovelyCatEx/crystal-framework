import {Segmented} from "antd";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import OverviewPage from "./TenantTireBenefitValueOverviewPage.tsx";
import ManagementPage from "./TenantTireBenefitValueManagementPage.tsx";

export default function BenefitValueContainer() {
    const {t} = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const viewMode = (searchParams.get('tab') as 'overview' | 'management') || 'overview';

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
                        { value: 'overview', label: t('pages.tenantTireBenefitValueManager.switch.overview') },
                        { value: 'management', label: t('pages.tenantTireBenefitValueManager.switch.management') },
                    ]}
                />
            </div>
            {viewMode === 'overview' ? <OverviewPage /> : <ManagementPage />}
        </>
    );
}
