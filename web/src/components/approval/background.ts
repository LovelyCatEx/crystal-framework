/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
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