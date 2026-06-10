import {forwardRef, useCallback, useImperativeHandle, useMemo, useRef, useState} from "react";
import {theme} from "antd";
import type {Descendant} from "slate";
import {createEditor, Editor, Element as SlateElement, Text, Transforms} from "slate";
import type {BaseEditor} from "slate";
import type {RenderElementProps, RenderLeafProps} from "slate-react";
import {Editable, ReactEditor, Slate, useFocused, useSelected, withReact} from "slate-react";
import type {ReactEditor as ReactEditorType} from "slate-react";
import {withHistory} from "slate-history";
import {useTranslation} from "react-i18next";
import {parseMessageChainXml} from "@/utils/message-chain-xml.ts";
import {ImageResource} from "@/components/ImageResource.tsx";
import type {MessageSegmentNode} from "./types.ts";

// --- Slate custom types ---

type ParagraphElement = { type: 'paragraph'; children: Descendant[] };
type AtElement = { type: 'at'; userId?: string; tenantId?: string; displayName?: string; children: [{ text: '' }] };
type LinkElement = { type: 'link'; href: string; title?: string; children: [{ text: '' }] };
type ImageElement = { type: 'image'; src: string; children: [{ text: '' }] };

type CustomElement = ParagraphElement | AtElement | LinkElement | ImageElement;
type CustomText = { text: string };

declare module 'slate' {
    interface CustomTypes {
        Editor: BaseEditor & ReactEditorType;
        Element: CustomElement;
        Text: CustomText;
    }
}

// --- Public handle ---

export interface VisualEditorHandle {
    insertSegment: (segment: MessageSegmentNode) => void;
}

// --- Props ---

interface VisualEditorProps {
    segments: MessageSegmentNode[];
    onChange: (segments: MessageSegmentNode[]) => void;
    disabled?: boolean;
    placeholder?: string;
    minRows?: number;
    maxRows?: number;
}

// --- Component ---

export const VisualEditor = forwardRef<VisualEditorHandle, VisualEditorProps>(
    function VisualEditor({segments, onChange, disabled, placeholder, minRows = 6, maxRows = 16}, ref) {
        const {t} = useTranslation();
        const {token} = theme.useToken();
        const editorRef = useRef<Editor>(null!);
        const [focused, setFocused] = useState(false);

        const editor = useMemo(() => {
            const e = withInlines(withHistory(withReact(createEditor())));
            editorRef.current = e;
            return e;
        }, []);

        const initialValue = useMemo(() => segmentsToSlate(segments), []);

        // Expose insertSegment to parent
        useImperativeHandle(ref, () => ({
            insertSegment(segment: MessageSegmentNode) {
                const node = segmentToSlateNode(segment);
                if (node) {
                    ReactEditor.focus(editor);
                    Transforms.insertNodes(editor, node);
                }
            },
        }), [editor]);

        const handleChange = useCallback((value: Descendant[]) => {
            onChange(slateToSegments(value));
        }, [onChange]);

        const renderElement = useCallback((props: RenderElementProps) => {
            const {attributes, children, element} = props;
            switch (element.type) {
                case 'at':
                    return <AtChip attributes={attributes} element={element}>{children}</AtChip>;
                case 'link':
                    return <LinkChip attributes={attributes} element={element}>{children}</LinkChip>;
                case 'image':
                    return <ImageChip attributes={attributes} element={element}>{children}</ImageChip>;
                default:
                    return <p {...attributes} className="m-0">{children}</p>;
            }
        }, []);

        const renderLeaf = useCallback((props: RenderLeafProps) => {
            return <span {...props.attributes}>{props.children}</span>;
        }, []);

        // Handle paste: parse XML from clipboard
        const handlePaste = useCallback((e: React.ClipboardEvent) => {
            const text = e.clipboardData.getData('text/plain');
            if (!text) return;

            if (/<(at|image|link|br)\s*[^>]*\/?>/.test(text)) {
                e.preventDefault();
                const pastedSegments = parseMessageChainXml(text);
                const nodes = segmentsToSlateInlineNodes(pastedSegments);
                Transforms.insertNodes(editor, nodes);
            }
        }, [editor]);

        const minHeight = minRows * 24;
        const maxHeight = maxRows * 24;
        const placeholderText = placeholder || t('components.messageChainEditor.placeholder');

        return (
            <Slate editor={editor} initialValue={initialValue} onChange={handleChange}>
                <Editable
                    readOnly={disabled}
                    placeholder={placeholderText}
                    renderElement={renderElement}
                    renderLeaf={renderLeaf}
                    onPaste={handlePaste}
                    onFocus={() => setFocused(true)}
                    onBlur={() => setFocused(false)}
                    className={[
                        'rounded-md px-3 py-2 leading-6 outline-none transition-colors',
                        'overflow-y-auto whitespace-pre-wrap break-words',
                        disabled ? 'bg-gray-50 cursor-not-allowed opacity-60' : 'cursor-text',
                    ].join(' ')}
                    style={{
                        minHeight: `${minHeight}px`,
                        maxHeight: `${maxHeight}px`,
                        border: `1px solid ${focused ? token.colorPrimary : token.colorBorder}`,
                        boxShadow: focused ? `0 0 0 2px ${token.colorPrimary}20` : 'none',
                    }}
                />
            </Slate>
        );
    }
);

// --- Inline Chip components ---

function AtChip({attributes, element, children}: any) {
    const selected = useSelected();
    const focused = useFocused();
    const {token} = theme.useToken();
    const label = `@${element.displayName || element.userId || '?'}`;
    const highlight = selected && focused;
    return (
        <span {...attributes} contentEditable={false} className="inline mx-px align-baseline">
            <span style={{
                color: token.colorPrimary,
                textDecoration: 'underline',
                textUnderlineOffset: '2px',
                borderRadius: '2px',
                boxShadow: highlight ? `0 0 0 2px ${token.colorPrimary}40` : 'none',
            }}>
                {label}
            </span>
            {children}
        </span>
    );
}

function LinkChip({attributes, element, children}: any) {
    const selected = useSelected();
    const focused = useFocused();
    const {token} = theme.useToken();
    const label = element.title || element.href;
    const highlight = selected && focused;
    return (
        <span {...attributes} contentEditable={false} className="inline mx-px align-baseline">
            <a style={{
                color: token.colorPrimary,
                textDecoration: 'underline',
                textUnderlineOffset: '2px',
                borderRadius: '2px',
                boxShadow: highlight ? `0 0 0 2px ${token.colorPrimary}40` : 'none',
                cursor: 'default',
            }}
               href={undefined}
               onClick={(e) => e.preventDefault()}
            >
                {label}
            </a>
            {children}
        </span>
    );
}

function ImageChip({attributes, element, children}: any) {
    const selected = useSelected();
    const focused = useFocused();
    const {token} = theme.useToken();
    const highlight = selected && focused;
    const isResource = element.src.startsWith('resource://');
    const resourceId = isResource ? element.src.replace('resource://', '') : null;

    return (
        <span {...attributes} contentEditable={false} className="inline-block mx-px align-middle">
            <span style={{
                display: 'inline-block',
                borderRadius: '4px',
                boxShadow: highlight ? `0 0 0 2px ${token.colorPrimary}40` : 'none',
            }}>
                {isResource ? (
                    <ImageResource fileEntityId={resourceId} width={80} preview={false}/>
                ) : (
                    <img src={element.src} alt="" style={{height: '40px', borderRadius: '4px'}}/>
                )}
            </span>
            {children}
        </span>
    );
}

// --- Slate plugin: treat at/link/image as inline void ---

function withInlines(editor: Editor): Editor {
    const {isInline, isVoid} = editor;

    editor.isInline = (element: CustomElement) => {
        return ['at', 'link', 'image'].includes(element.type) || isInline(element);
    };

    editor.isVoid = (element: CustomElement) => {
        return ['at', 'link', 'image'].includes(element.type) || isVoid(element);
    };

    return editor;
}

// --- Conversion: MessageSegmentNode[] ↔ Slate Descendant[] ---

function segmentsToSlate(segments: MessageSegmentNode[]): Descendant[] {
    if (segments.length === 0) {
        return [{type: 'paragraph', children: [{text: ''}]}];
    }
    return splitByNewline(segments).map((line) => ({
        type: 'paragraph' as const,
        children: line.length === 0 ? [{text: ''}] : segmentsToSlateInlineNodes(line),
    }));
}

function segmentsToSlateInlineNodes(segments: MessageSegmentNode[]): (CustomElement | CustomText)[] {
    const nodes: (CustomElement | CustomText)[] = [];
    for (const seg of segments) {
        const node = segmentToSlateNode(seg);
        if (node) {
            if (Array.isArray(node)) nodes.push(...node);
            else nodes.push(node);
        }
    }
    return nodes.length > 0 ? nodes : [{text: ''}];
}

function segmentToSlateNode(segment: MessageSegmentNode): CustomElement | CustomText | null {
    switch (segment.type) {
        case 'text':
            return {text: segment.text};
        case 'at':
            return {type: 'at', userId: segment.userId, tenantId: segment.tenantId, displayName: segment.displayName, children: [{text: ''}]};
        case 'link':
            return {type: 'link', href: segment.href, title: segment.title, children: [{text: ''}]};
        case 'image':
            return {type: 'image', src: segment.src, children: [{text: ''}]};
        case 'br':
            return null; // Handled by paragraph splitting
    }
}

function slateToSegments(value: Descendant[]): MessageSegmentNode[] {
    const segments: MessageSegmentNode[] = [];
    for (let i = 0; i < value.length; i++) {
        const node = value[i];
        if (SlateElement.isElement(node) && node.type === 'paragraph') {
            for (const child of node.children) {
                if (Text.isText(child)) {
                    if (child.text) segments.push({type: 'text', text: child.text});
                } else if (SlateElement.isElement(child)) {
                    switch (child.type) {
                        case 'at':
                            segments.push({type: 'at', userId: child.userId, tenantId: child.tenantId, displayName: child.displayName});
                            break;
                        case 'link':
                            segments.push({type: 'link', href: child.href, title: child.title});
                            break;
                        case 'image':
                            segments.push({type: 'image', src: child.src});
                            break;
                    }
                }
            }
            // Add br between paragraphs (not after the last one)
            if (i < value.length - 1) {
                segments.push({type: 'br'});
            }
        }
    }
    return segments;
}

/** Split segments by 'br' into lines (for paragraph mapping) */
function splitByNewline(segments: MessageSegmentNode[]): MessageSegmentNode[][] {
    const lines: MessageSegmentNode[][] = [[]];
    for (const seg of segments) {
        if (seg.type === 'br') {
            lines.push([]);
        } else {
            lines[lines.length - 1].push(seg);
        }
    }
    return lines;
}