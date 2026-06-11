import {Button, Space} from "antd";
import {LinkOutlined, PictureOutlined, UserOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {InsertPopover} from "./InsertPopover.tsx";
import type {MessageSegmentNode, MessageSegmentType} from "./types.ts";

interface InsertToolbarProps {
    allowedSegments?: MessageSegmentType[];
    onInsert: (segment: MessageSegmentNode) => void;
    disabled?: boolean;
}

const DEFAULT_SEGMENTS: MessageSegmentType[] = ['at', 'link', 'image', 'br'];

export function InsertToolbar({allowedSegments = DEFAULT_SEGMENTS, onInsert, disabled}: InsertToolbarProps) {
    const {t} = useTranslation();

    const buttons: { type: MessageSegmentType; icon: React.ReactNode; label: string }[] = [
        {type: 'at', icon: <UserOutlined/>, label: t('components.messageChainEditor.insert.at')},
        {type: 'link', icon: <LinkOutlined/>, label: t('components.messageChainEditor.insert.link')},
        {type: 'image', icon: <PictureOutlined/>, label: t('components.messageChainEditor.insert.image')},
    ];

    return (
        <Space size={4} wrap>
            {buttons
                .filter((btn) => allowedSegments.includes(btn.type))
                .map((btn) => (
                    <InsertPopover
                        key={btn.type}
                        segmentType={btn.type}
                        onInsert={onInsert}
                        disabled={disabled}
                    >
                        <Button
                            type="text"
                            icon={btn.icon}
                            disabled={disabled}
                        >
                            {btn.label}
                        </Button>
                    </InsertPopover>
                ))}
        </Space>
    );
}
