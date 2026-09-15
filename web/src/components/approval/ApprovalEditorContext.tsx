/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
