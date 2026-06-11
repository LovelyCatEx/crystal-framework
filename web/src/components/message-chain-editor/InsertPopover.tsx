import {Button, Form, Input, Popover, Select} from "antd";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import type {MessageSegmentNode, MessageSegmentType} from "./types.ts";

type ImageScheme = 'https://' | 'http://' | 'resource://';

const IMAGE_SCHEME_OPTIONS: { value: ImageScheme; label: string }[] = [
    {value: 'https://', label: 'https://'},
    {value: 'http://', label: 'http://'},
    {value: 'resource://', label: 'resource://'},
];

interface InsertPopoverProps {
    segmentType: MessageSegmentType;
    onInsert: (segment: MessageSegmentNode) => void;
    children: React.ReactNode;
    disabled?: boolean;
}

export function InsertPopover({segmentType, onInsert, children, disabled}: InsertPopoverProps) {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const [form] = Form.useForm();

    if (segmentType === 'br') {
        return (
            <span onClick={() => !disabled && onInsert({type: 'br'})}>
                {children}
            </span>
        );
    }

    const handleConfirm = () => {
        form.validateFields().then((values) => {
            const segment = buildSegmentFromForm(segmentType, values);
            if (segment) {
                onInsert(segment);
                form.resetFields();
                setOpen(false);
            }
        }).catch(() => {});
    };

    const handleCancel = () => {
        form.resetFields();
        setOpen(false);
    };

    const content = (
        <Form form={form} layout="vertical" style={{width: getPopoverWidth(segmentType)}}>
            {renderFields(segmentType, t)}
            <div className="flex justify-end gap-2 mt-2">
                <Button onClick={handleCancel}>
                    {t('components.messageChainEditor.cancel')}
                </Button>
                <Button type="primary" onClick={handleConfirm}>
                    {t('components.messageChainEditor.confirm')}
                </Button>
            </div>
        </Form>
    );

    return (
        <Popover
            content={content}
            trigger="click"
            open={open}
            onOpenChange={(v) => !disabled && setOpen(v)}
            destroyTooltipOnHide
        >
            {children}
        </Popover>
    );
}

function renderFields(type: MessageSegmentType, t: (key: string) => string) {
    switch (type) {
        case 'at':
            return (
                <>
                    <Form.Item name="userId" label={t('components.messageChainEditor.fields.userId')}>
                        <Input placeholder="123"/>
                    </Form.Item>
                    <Form.Item name="tenantId" label={t('components.messageChainEditor.fields.tenantId')}>
                        <Input placeholder="456"/>
                    </Form.Item>
                    <Form.Item name="displayName" label={t('components.messageChainEditor.fields.displayName')}>
                        <Input/>
                    </Form.Item>
                </>
            );
        case 'link':
            return (
                <>
                    <Form.Item
                        name="href"
                        label={t('components.messageChainEditor.fields.href')}
                        rules={[{required: true, message: t('components.messageChainEditor.fields.href')}]}
                    >
                        <Input placeholder="https://..."/>
                    </Form.Item>
                    <Form.Item name="title" label={t('components.messageChainEditor.fields.title')}>
                        <Input/>
                    </Form.Item>
                </>
            );
        case 'image':
            return <ImageSrcField t={t}/>;
        default:
            return null;
    }
}

function ImageSrcField({t}: { t: (key: string) => string }) {
    const form = Form.useFormInstance();
    const [scheme, setScheme] = useState<ImageScheme>('https://');

    const handleSchemeChange = (v: ImageScheme) => {
        setScheme(v);
        form.setFieldsValue({__scheme: v});
    };

    const handleValueChange = (value: string) => {
        const detected = detectScheme(value.trim());
        if (detected) {
            setScheme(detected.scheme);
            form.setFieldsValue({src: detected.value, __scheme: detected.scheme});
        }
    };

    const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
        const pasted = e.clipboardData.getData('text/plain').trim();
        if (!pasted) return;

        const detected = detectScheme(pasted);
        if (detected) {
            e.preventDefault();
            setScheme(detected.scheme);
            form.setFieldsValue({src: detected.value, __scheme: detected.scheme});
        }
    };

    const schemeSelector = (
        <Select
            value={scheme}
            onChange={handleSchemeChange}
            options={IMAGE_SCHEME_OPTIONS}
            style={{width: 110}}
            popupMatchSelectWidth={false}
        />
    );

    return (
        <>
            <Form.Item name="__scheme" hidden initialValue="https://">
                <Input/>
            </Form.Item>
            <Form.Item
                name="src"
                label={t('components.messageChainEditor.fields.src')}
                rules={[{required: true, message: t('components.messageChainEditor.fields.src')}]}
            >
                <Input
                    addonBefore={schemeSelector}
                    placeholder={scheme === 'resource://' ? 'file-id' : 'example.com/image.png'}
                    onPaste={handlePaste}
                    onChange={(e) => handleValueChange(e.target.value)}
                />
            </Form.Item>
        </>
    );
}

/** Detect and strip scheme prefix from pasted value */
function detectScheme(value: string): { scheme: ImageScheme; value: string } | null {
    if (value.startsWith('https://')) {
        return {scheme: 'https://', value: value.replace('https://', '')};
    }
    if (value.startsWith('http://')) {
        return {scheme: 'http://', value: value.replace('http://', '')};
    }
    if (value.startsWith('resource://')) {
        return {scheme: 'resource://', value: value.replace('resource://', '')};
    }
    // No scheme detected: pure digits → resource, otherwise https
    if (/^\d+$/.test(value)) {
        return {scheme: 'resource://', value};
    }
    return null;
}

function getPopoverWidth(type: MessageSegmentType): number {
    switch (type) {
        case 'at':
            return 280;
        case 'link':
        case 'image':
            return 480;
        default:
            return 320;
    }
}

function buildSegmentFromForm(type: MessageSegmentType, values: Record<string, string>): MessageSegmentNode | null {
    switch (type) {
        case 'at':
            return {
                type: 'at',
                userId: values.userId || undefined,
                tenantId: values.tenantId || undefined,
                displayName: values.displayName || undefined,
            };
        case 'link':
            return values.href ? {type: 'link', href: values.href, title: values.title || undefined} : null;
        case 'image': {
            if (!values.src) return null;
            const scheme = (values.__scheme || 'https://') as ImageScheme;
            return {type: 'image', src: scheme + values.src};
        }
        default:
            return null;
    }
}
