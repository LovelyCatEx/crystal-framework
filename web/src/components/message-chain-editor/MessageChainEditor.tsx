import {Button, Input, Modal, Space} from "antd";
import {CodeOutlined, EditOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import TextArea from "antd/es/input/TextArea";
import {VisualEditor} from "./VisualEditor.tsx";
import type {VisualEditorHandle} from "./VisualEditor.tsx";
import {InsertToolbar} from "./InsertToolbar.tsx";
import {parseMessageChainXml, serializeMessageChain} from "@/utils/message-chain-xml.ts";
import type {MessageChainEditorProps, MessageSegmentNode} from "./types.ts";

type EditorMode = 'visual' | 'source';

/**
 * MessageChain XML editor.
 * Renders as a readonly Input with an edit button; clicking opens a Modal
 * containing the full visual/source editor with insert toolbar.
 * Compatible with Ant Design Form.Item (value + onChange string interface).
 */
export function MessageChainEditor({
    value = '',
    onChange,
    disabled,
    placeholder,
    allowedSegments,
    minRows = 6,
    maxRows = 16,
}: MessageChainEditorProps) {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const [mode, setMode] = useState<EditorMode>('visual');
    const [segments, setSegments] = useState<MessageSegmentNode[]>(() => parseMessageChainXml(value));
    const [sourceText, setSourceText] = useState(value);
    const visualEditorRef = useRef<VisualEditorHandle>(null);

    // Sync external value into internal state when modal opens
    useEffect(() => {
        if (open) {
            setSegments(parseMessageChainXml(value));
            setSourceText(value);
        }
    }, [open, value]);

    // Visual mode: segments changed
    const handleSegmentsChange = useCallback((newSegments: MessageSegmentNode[]) => {
        setSegments(newSegments);
        setSourceText(serializeMessageChain(newSegments));
    }, []);

    // Source mode: text changed
    const handleSourceChange = useCallback((text: string) => {
        setSourceText(text);
    }, []);

    // Mode switch
    const switchMode = useCallback((newMode: EditorMode) => {
        if (newMode === 'visual' && mode === 'source') {
            setSegments(parseMessageChainXml(sourceText));
        } else if (newMode === 'source' && mode === 'visual') {
            setSourceText(serializeMessageChain(segments));
        }
        setMode(newMode);
    }, [mode, sourceText, segments]);

    // Insert segment from toolbar
    const handleInsertSegment = useCallback((segment: MessageSegmentNode) => {
        if (mode === 'visual') {
            visualEditorRef.current?.insertSegment(segment);
        } else {
            const xml = serializeMessageChain([segment]);
            setSourceText((prev) => prev + xml);
        }
    }, [mode]);

    // Confirm: emit final value
    const handleOk = () => {
        const finalXml = mode === 'visual' ? serializeMessageChain(segments) : sourceText;
        onChange?.(finalXml);
        setOpen(false);
    };

    const handleCancel = () => {
        setOpen(false);
    };

    const displayValue = value || '';

    return (
        <>
            <Input
                value={displayValue}
                readOnly
                disabled={disabled}
                placeholder={placeholder || t('components.messageChainEditor.placeholder')}
                suffix={
                    <Button
                        type="text"
                        icon={<EditOutlined/>}
                        disabled={disabled}
                        onClick={() => setOpen(true)}
                    />
                }
                onClick={() => !disabled && setOpen(true)}
                className="cursor-pointer"
            />
            <Modal
                title={t('components.messageChainEditor.modalTitle')}
                open={open}
                onOk={handleOk}
                onCancel={handleCancel}
                okText={t('components.messageChainEditor.modalOk')}
                cancelText={t('components.messageChainEditor.cancel')}
                width={720}
                centered
                destroyOnHidden
                maskClosable={false}
                closable={false}
            >
                {/* Toolbar */}
                <div className="flex items-center justify-between mb-3">
                    <InsertToolbar
                        allowedSegments={allowedSegments}
                        onInsert={handleInsertSegment}
                    />
                    <Space size={4}>
                        <Button
                            type={mode === 'visual' ? 'primary' : 'text'}
                            icon={<EditOutlined/>}
                            onClick={() => switchMode('visual')}
                        >
                            {t('components.messageChainEditor.visualMode')}
                        </Button>
                        <Button
                            type={mode === 'source' ? 'primary' : 'text'}
                            icon={<CodeOutlined/>}
                            onClick={() => switchMode('source')}
                        >
                            {t('components.messageChainEditor.sourceMode')}
                        </Button>
                    </Space>
                </div>

                {/* Editor area */}
                <div>
                    {mode === 'visual' ? (
                        <VisualEditor
                            ref={visualEditorRef}
                            segments={segments}
                            onChange={handleSegmentsChange}
                            placeholder={placeholder}
                            minRows={minRows}
                            maxRows={maxRows}
                        />
                    ) : (
                        <TextArea
                            value={sourceText}
                            onChange={(e) => handleSourceChange(e.target.value)}
                            placeholder={placeholder || t('components.messageChainEditor.placeholder')}
                            autoSize={{minRows, maxRows}}
                            className="font-mono"
                        />
                    )}
                </div>
            </Modal>
        </>
    );
}
