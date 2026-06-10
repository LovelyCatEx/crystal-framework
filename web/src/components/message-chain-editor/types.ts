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
