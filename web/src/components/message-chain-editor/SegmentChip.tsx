import {Tag} from "antd";
import {CloseOutlined} from "@ant-design/icons";
import type {MessageSegmentNode} from "./types.ts";

interface SegmentChipProps {
    segment: MessageSegmentNode;
    onDelete?: () => void;
    disabled?: boolean;
}

export function SegmentChip({segment, onDelete, disabled}: SegmentChipProps) {
    if (segment.type === 'text') return null;

    const chipConfig = getChipConfig(segment);

    return (
        <Tag
            color={chipConfig.color}
            className="inline-flex items-center mx-0.5 select-none cursor-default"
            style={{verticalAlign: 'middle'}}
        >
            <span className="text-xs">{chipConfig.label}</span>
            {!disabled && onDelete && (
                <CloseOutlined
                    className="ml-1 text-[10px] cursor-pointer opacity-60 hover:opacity-100"
                    onClick={(e) => {
                        e.stopPropagation();
                        onDelete();
                    }}
                />
            )}
        </Tag>
    );
}

function getChipConfig(segment: MessageSegmentNode): { color: string; label: string } {
    switch (segment.type) {
        case 'at':
            return {
                color: 'blue',
                label: `@${segment.displayName || segment.userId || '?'}`,
            };
        case 'link':
            return {
                color: 'cyan',
                label: `🔗 ${segment.title || truncate(segment.href, 24)}`,
            };
        case 'image':
            return {
                color: 'green',
                label: `🖼 ${truncate(segment.src, 20)}`,
            };
        case 'br':
            return {color: 'default', label: '↵'};
        default:
            return {color: 'default', label: '?'};
    }
}

function truncate(str: string, maxLen: number): string {
    return str.length > maxLen ? str.substring(0, maxLen) + '…' : str;
}
