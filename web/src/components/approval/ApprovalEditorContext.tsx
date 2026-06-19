import {createContext, useContext} from "react";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

export interface ApprovalEditorContextValue {
    scope: number;
    scopeId: string;
}

export const ApprovalEditorContext = createContext<ApprovalEditorContextValue>({
    scope: ResourceScope.SYSTEM,
    scopeId: '',
});

export function useApprovalEditorContext() {
    return useContext(ApprovalEditorContext);
}
