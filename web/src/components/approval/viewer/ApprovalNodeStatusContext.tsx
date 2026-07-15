import {createContext, type ReactNode, useContext} from "react";
import type {ApprovalNodeStateVO} from "@/types/approval/approval-flow-instance-details.types.ts";

export interface ApprovalNodeStatusContextValue {
    getNodeStateById: (nodeId: string) => ApprovalNodeStateVO | null;
}

const ApprovalNodeStatusContext = createContext<ApprovalNodeStatusContextValue | null>(null);

export function ApprovalNodeStatusProvider({value, children}: {
    value: ApprovalNodeStatusContextValue;
    children: ReactNode;
}) {
    return (
        <ApprovalNodeStatusContext.Provider value={value}>
            {children}
        </ApprovalNodeStatusContext.Provider>
    );
}

export function useApprovalNodeStatus(nodeId: string): ApprovalNodeStateVO | null {
    const ctx = useContext(ApprovalNodeStatusContext);
    if (!ctx) return null;
    return ctx.getNodeStateById(nodeId);
}
