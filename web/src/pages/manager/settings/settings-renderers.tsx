import type {ReactNode} from "react";
import type {FormInstance} from "antd";
import {Input} from "antd";
import type {SystemSettingsSchema} from "@/types/system/system-settings.types.ts";
import {ColorPickerInput} from "@/components/ColorPickerInput.tsx";
import {TestSendEmailButton} from "@/components/TestSendEmailButton.tsx";
import {TestSendLarkButton} from "@/components/TestSendLarkButton.tsx";

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

const passwordRenderer: SettingsItemRenderer = ({schema, loading}) => (
    <Input.Password
        className="rounded-lg h-10"
        placeholder={schema.defaultValue ?? ''}
        disabled={loading}
        autoComplete="new-password"
    />
);

export const settingsItemRenderers = new Map<string, SettingsItemRenderer>([
    ['basic.waterMark.fontColor', ({loading}) => <ColorPickerInput disabled={loading} />],
    ['mail.smtp.password', passwordRenderer],
    ['messageChannel.lark.appSecret', passwordRenderer],
]);

export const settingsGroupExtraRenderers = new Map<string, SettingsGroupExtraRenderer>([
    ['mail.smtp', () => (
        <div className="flex justify-end">
            <TestSendEmailButton/>
        </div>
    )],
    ['messageChannel.lark', () => (
        <div className="flex justify-end">
            <TestSendLarkButton/>
        </div>
    )],
]);
