import {Navigate, useLocation} from "react-router-dom";
import React from "react";
import {getUserAuthentication} from "../utils/token.utils.ts";

export function RequireAuthComponent({ children }: { children: React.ReactNode }) {
    const authentication = getUserAuthentication();
    const location = useLocation();

    if (!authentication) {
        return <Navigate to={`/auth/login?redirectTo=${location.pathname}`} state={{ from: location.pathname }} replace />;
    }

    return children;
}