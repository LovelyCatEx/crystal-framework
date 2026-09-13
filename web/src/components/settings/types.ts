/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {ReactNode} from "react";
import type {FormInstance} from "antd";
import type {SystemSettingsSchema} from "@/types/system/system-settings.types.ts";

export interface SettingsItemRenderContext {
    settingsKey: string;
    schema: SystemSettingsSchema;
    loading?: boolean;
}

export type SettingsItemRenderer = (ctx: SettingsItemRenderContext) => ReactNode;

export interface SettingsGroupExtraRenderContext {
    group: string;
    form: FormInstance;
}

export type SettingsGroupExtraRenderer = (ctx: SettingsGroupExtraRenderContext) => ReactNode;
