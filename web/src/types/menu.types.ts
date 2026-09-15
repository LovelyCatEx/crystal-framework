/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";

export interface MenuGroup {
    name: string;
    icon: JSX.Element | React.ReactNode;
    label: string;
}

export interface MenuItem {
    path: string;
    icon: JSX.Element | React.ReactNode;
    label: string;
    page?: JSX.Element | React.ReactNode;
    group?: string;
}