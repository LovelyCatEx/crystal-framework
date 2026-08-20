import type React from "react";
import {createContext, useContext} from "react";
import useSWR from "swr";
import {getSystemIntegratedInfo} from "@/api/system/system-integrated.api.ts";
import type {MaintenanceInfoVO, SystemIntegratedInfoVO, WaterMarkInfo} from "@/types/system/system-integrated.types.ts";

/** SWR key of the shared system-integrated snapshot. Exported so writers of the underlying settings
 *  (system settings save, maintenance toggle) can force an immediate revalidation without waiting
 *  for `revalidateOnFocus`. */
export const SWR_KEY_SYSTEM_INTEGRATED_INFO = 'systemIntegratedInfo';

interface SystemIntegratedContextValue {
    integratedInfo: SystemIntegratedInfoVO | undefined;
    maintenanceInfo: MaintenanceInfoVO | undefined;
    waterMarkInfo: WaterMarkInfo | undefined;
    disabledModules: string[];
    isModuleEnabled: (moduleKey: string) => boolean;
    isLoading: boolean;
    error: Error | undefined;
    mutate: () => void;
}

const SystemIntegratedContext = createContext<SystemIntegratedContextValue | undefined>(undefined);

export function SystemIntegratedProvider({children}: { children: React.ReactNode }) {
    const {data, isLoading, error, mutate} = useSWR<SystemIntegratedInfoVO>(
        SWR_KEY_SYSTEM_INTEGRATED_INFO,
        () => getSystemIntegratedInfo().then((res) => res.data!),
        {revalidateOnFocus: true},
    );

    const value: SystemIntegratedContextValue = {
        integratedInfo: data,
        maintenanceInfo: data?.maintenance,
        waterMarkInfo: data?.waterMark,
        disabledModules: data?.disabledModules ?? [],
        isModuleEnabled: (moduleKey: string) => !(data?.disabledModules ?? []).includes(moduleKey),
        isLoading,
        error,
        mutate,
    };

    return (
        <SystemIntegratedContext.Provider value={value}>
            {children}
        </SystemIntegratedContext.Provider>
    );
}

export function useSystemIntegrated() {
    const context = useContext(SystemIntegratedContext);
    if (context === undefined) {
        throw new Error('useSystemIntegrated must be used within SystemIntegratedProvider');
    }
    return context;
}
