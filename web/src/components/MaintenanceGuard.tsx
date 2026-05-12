import type React from "react";
import {Result, Spin} from "antd";
import {MaintenancePage} from "@/pages/MaintenancePage.tsx";
import {useMaintenanceStatus} from "@/compositions/use-maintenance.ts";
import {useTranslation} from "react-i18next";

/**
 * Wraps children with a maintenance mode check.
 *
 * - Loading: full-screen spinner (blocks children from mounting)
 * - Error (cannot reach server): fallback error page
 * - Maintenance enabled AND no access: maintenance page
 * - Maintenance disabled OR user has access: render children
 */
export function MaintenanceGuard({children}: { children: React.ReactNode }) {
    const {t} = useTranslation();
    const {maintenanceMode, canAccess, isLoading, error} = useMaintenanceStatus();

    // Still fetching — keep showing spinner, never render children
    if (isLoading || (maintenanceMode === undefined && !error)) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <Spin size="large"/>
            </div>
        );
    }

    // Request failed — show error fallback
    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <Result
                    status="500"
                    title={t('pages.serviceUnavailable.title')}
                    subTitle={t('pages.serviceUnavailable.description')}
                />
            </div>
        );
    }

    // Maintenance mode is on AND user does NOT have access
    if (maintenanceMode && !canAccess) {
        return <MaintenancePage/>;
    }

    // All clear (not in maintenance, or user has access privilege)
    return children;
}
