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
