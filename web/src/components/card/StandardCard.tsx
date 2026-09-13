/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Card} from "antd";
import type {HTMLAttributes} from "react";

export function StandardCard(props: HTMLAttributes<HTMLDivElement>) {
    return (
        <Card {...props} className={`border-none shadow-sm rounded-2xl overflow-hidden ${props.className}`}>
            {props.children}
        </Card>
    )
}