import type React from "react";
import {createContext, useContext} from "react";
import useSWR from "swr";
import {getSystemIntegratedInfo} from "@/api/system-integrated.api.ts";
import type {
    SystemIntegratedInfoVO,
    MaintenanceInfoVO,
    WaterMarkInfo
} from "@/types/system-integrated.types.ts";

interface SystemIntegratedContextValue {
    integratedInfo: SystemIntegratedInfoVO | undefined;
    maintenanceInfo: MaintenanceInfoVO | undefined;
    waterMarkInfo: WaterMarkInfo | undefined;
    isLoading: boolean;
    error: Error | undefined;
    mutate: () => void;
}

const SystemIntegratedContext = createContext<SystemIntegratedContextValue | undefined>(undefined);

export function SystemIntegratedProvider({children}: { children: React.ReactNode }) {
    const {data, isLoading, error, mutate} = useSWR<SystemIntegratedInfoVO>(
        'systemIntegratedInfo',
        () => getSystemIntegratedInfo().then((res) => res.data!),
    );

    const value: SystemIntegratedContextValue = {
        integratedInfo: data,
        maintenanceInfo: data?.maintenance,
        waterMarkInfo: data?.waterMark,
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
