/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseSchemes} from "rete";
import { AreaPlugin } from "rete-area-plugin";
import './background.css';

export function applyApprovalFlowEditorAreaBackground<S extends BaseSchemes, K>(
    area: AreaPlugin<S, K>
) {
    const background = document.createElement("div");

    background.classList.add("area-background");
    background.classList.add("area-fill");

    area.area.content.add(background);
}