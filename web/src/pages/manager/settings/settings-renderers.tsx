import {Input} from "antd";
import {ColorPickerInput} from "@/components/ColorPickerInput.tsx";
import {TestSendEmailButton} from "@/components/TestSendEmailButton.tsx";
import {TestSendLarkButton} from "@/components/TestSendLarkButton.tsx";
import type {
    SettingsGroupExtraRenderer,
    SettingsItemRenderer,
} from "@/components/settings/types.ts";

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
    ['oauth.github.clientSecret', passwordRenderer],
    ['oauth.google.clientSecret', passwordRenderer],
    ['oauth.oicq.clientSecret', passwordRenderer],
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
