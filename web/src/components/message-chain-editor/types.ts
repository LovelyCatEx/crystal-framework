/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export type MessageSegmentType = 'at' | 'image' | 'link' | 'br';

export type MessageSegmentNode =
    | { type: 'text'; text: string }
    | { type: 'at'; userId?: string; tenantId?: string; displayName?: string }
    | { type: 'image'; src: string }
    | { type: 'link'; href: string; title?: string }
    | { type: 'br' }

export interface MessageChainEditorProps {
    value?: string;
    onChange?: (xml: string) => void;
    disabled?: boolean;
    placeholder?: string;
    /** Control which special segment types are available in the toolbar */
    allowedSegments?: MessageSegmentType[];
    minRows?: number;
    maxRows?: number;
}
