import type React from "react";
import {Result, Spin} from "antd";
import {MaintenancePage} from "@/pages/MaintenancePage.tsx";
import {useMaintenanceStatus} from "@/compositions/use-maintenance.ts";
import {useTranslation} from "react-i18next";
import {MaintenanceBanner} from "@/components/MaintenanceBanner.tsx";

export type MaintenanceStatus = 'disconnected' | 'maintenance' | 'loading' | 'pass';
export type MaintenanceGuardMode = 'fromApi' | 'fromData';

interface MaintenanceGuardProps {
    children: React.ReactNode;
    mode?: MaintenanceGuardMode;
    status?: MaintenanceStatus;
}

/**
 * Wraps children with a maintenance mode check.
 *
 * Modes:
 * - fromApi (default): Fetches maintenance status from API (legacy behavior)
 * - fromData: Uses status prop provided by parent
 *
 * Status (when mode is 'fromData'):
 * - loading: full-screen spinner
 * - disconnected: 500 error page
 * - maintenance: maintenance page
 * - pass: render children
 */
export function MaintenanceGuard({children, mode = 'fromApi', status}: MaintenanceGuardProps) {
    const {t} = useTranslation();

    // Mode: fromApi (legacy behavior)
    if (mode === 'fromApi') {
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
        return (
            <>
                {children}
                <MaintenanceBanner />
            </>
        );
    }

    // Mode: fromData (use provided status)
    if (mode === 'fromData') {
        if (!status) {
            throw new Error('MaintenanceGuard: status prop is required when mode is "fromData"');
        }

        switch (status) {
            case 'loading':
                return (
                    <div className="min-h-screen flex items-center justify-center">
                        <Spin size="large"/>
                    </div>
                );

            case 'disconnected':
                return (
                    <div className="min-h-screen flex items-center justify-center">
                        <Result
                            status="500"
                            title={t('pages.serviceUnavailable.title')}
                            subTitle={t('pages.serviceUnavailable.description')}
                        />
                    </div>
                );

            case 'maintenance':
                return <MaintenancePage/>;

            case 'pass':
                return (
                    <>
                        {children}
                        <MaintenanceBanner />
                    </>
                );

            default:
                return null;
        }
    }

    return null;
}
