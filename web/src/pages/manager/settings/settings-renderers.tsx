/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ColorPickerInput} from "@/components/input/ColorPickerInput.tsx";
import {TestSendEmailButton} from "@/components/TestSendEmailButton.tsx";
import {TestSendLarkButton} from "@/components/TestSendLarkButton.tsx";
import {TestSendWebSocketButton} from "@/components/TestSendWebSocketButton.tsx";
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
    ['module.webSocket', () => (
        <div className="flex justify-end">
            <TestSendWebSocketButton/>
        </div>
    )],
]);
