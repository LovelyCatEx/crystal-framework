import {createContext, useContext} from "react";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

interface ApprovalEditorContextValue {
    scope: number;
    scopeId: string;
}

const ApprovalEditorContext = createContext<ApprovalEditorContextValue>({
    scope: ResourceScope.SYSTEM,
    scopeId: '',
});

export const ApprovalEditorProvider = ApprovalEditorContext.Provider;

export function useApprovalEditorContext() {
    return useContext(ApprovalEditorContext);
}
