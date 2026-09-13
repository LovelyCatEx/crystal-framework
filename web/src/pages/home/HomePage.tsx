/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useNavigate} from "react-router-dom";
import {useEffect} from "react";
import {ProjectDisplayName} from "@/global/global-settings.ts";
import {getUserAuthentication} from "@/utils/token.utils.ts";
import {menuPathLogin} from "@/router/paths.ts";

export function HomePage() {
    const navigate = useNavigate();

    useEffect(() => {
        document.title = ProjectDisplayName
    }, []);

    useEffect(() => {
        const auth = getUserAuthentication();
        if (!auth || auth.expired) {
            navigate(menuPathLogin);
        }
    }, [navigate]);

    return (
        <>
        </>
    )
}