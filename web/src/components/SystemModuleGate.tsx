import type {ReactNode} from "react";
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {isModuleDisabled, type SystemModuleKeyValue} from "@/router/system-module-menu-paths.ts";

export interface SystemModuleGateProps {
    /** Module key mirroring backend `SystemModulePathConstants.<Module>.KEY`. */
    moduleKey: SystemModuleKeyValue;
    children: ReactNode;
}

/**
 * Renders `children` only when the given system module is enabled. Renders nothing when
 * the backend reports the module as disabled via `/system/integratedInfo`.
 *
 * Note: while the integrated info is loading (initial cold render) `disabledModules` is
 * an empty array, so children render optimistically. Once the fetch resolves the gate
 * will re-render and hide children if the module is disabled.
 */
export function SystemModuleGate({moduleKey, children}: SystemModuleGateProps) {
    const {disabledModules} = useSystemIntegrated();
    if (isModuleDisabled(disabledModules, moduleKey)) {
        return null;
    }
    return <>{children}</>;
}
