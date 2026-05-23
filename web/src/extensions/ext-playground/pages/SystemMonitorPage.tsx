import {useTranslation} from "react-i18next";
import {SystemMetrics} from "@/components/dashboard/SystemMetrics.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";

export function SystemMonitorPage() {
    const {t} = useTranslation();

    return (
        <>
            <ActionBarComponent
                title={t('pages.systemMonitor.title')}
                subtitle={t('pages.systemMonitor.subtitle')}
            />
            <SystemMetrics />
        </>
    );
}
