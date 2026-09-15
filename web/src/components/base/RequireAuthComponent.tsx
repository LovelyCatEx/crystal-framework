/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Navigate, useLocation} from "react-router-dom";
import React from "react";
import {getUserAuthentication} from "@/utils/token.utils.ts";

export function RequireAuthComponent({ children }: { children: React.ReactNode }) {
    const authentication = getUserAuthentication();
    const location = useLocation();

    if (!authentication) {
        return <Navigate to={`/auth/login?redirectTo=${location.pathname}`} state={{ from: location.pathname }} replace />;
    }

    return children;
}