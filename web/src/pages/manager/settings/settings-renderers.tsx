import {ColorPickerInput} from "@/components/input/ColorPickerInput.tsx";
import {TestSendEmailButton} from "@/components/TestSendEmailButton.tsx";
import {TestSendLarkButton} from "@/components/TestSendLarkButton.tsx";
import type {
    SettingsGroupExtraRenderer,
    SettingsItemRenderer,
} from "@/components/settings/types.ts";

export const settingsItemRenderers = new Map<string, SettingsItemRenderer>([
    ['basic.waterMark.fontColor', ({loading}) => <ColorPickerInput disabled={loading} />],
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
