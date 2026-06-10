import type {MessageSegmentNode} from "@/components/message-chain-editor/types.ts";

const SUPPORTED_TAGS = new Set(['at', 'image', 'link', 'br']);

/**
 * Parse MessageChain XML string into segment nodes.
 * Mirrors backend MessageChainXmlParser logic exactly.
 */
export function parseMessageChainXml(xml: string): MessageSegmentNode[] {
    if (!xml) return [];

    const segments: MessageSegmentNode[] = [];
    let textBuffer = '';
    let i = 0;

    function flushText() {
        if (textBuffer) {
            segments.push({type: 'text', text: decodeEntities(textBuffer)});
            textBuffer = '';
        }
    }

    while (i < xml.length) {
        if (xml[i] !== '<') {
            textBuffer += xml[i];
            i++;
            continue;
        }

        const tagEnd = xml.indexOf('>', i + 1);
        if (tagEnd < 0) {
            textBuffer += xml.substring(i);
            i = xml.length;
            continue;
        }

        const raw = xml.substring(i + 1, tagEnd).trim();
        const tagName = raw.match(/^[^\s/]+/)?.[0]?.toLowerCase() ?? '';

        if (!SUPPORTED_TAGS.has(tagName)) {
            textBuffer += xml.substring(i, tagEnd + 1);
            i = tagEnd + 1;
            continue;
        }

        const attrs = parseAttrs(raw.substring(tagName.length));
        const segment = buildSegment(tagName, attrs);
        if (segment) {
            flushText();
            segments.push(segment);
        }
        i = tagEnd + 1;
    }

    flushText();
    return segments;
}

/**
 * Serialize segment nodes back to MessageChain XML string.
 * Mirrors backend MessageChainXmlWriter logic exactly.
 */
export function serializeMessageChain(segments: MessageSegmentNode[]): string {
    let result = '';
    for (const seg of segments) {
        switch (seg.type) {
            case 'text':
                result += escapeText(seg.text);
                break;
            case 'at':
                result += '<at';
                if (seg.userId) result += ` user="${escapeAttr(seg.userId)}"`;
                if (seg.tenantId) result += ` tenant="${escapeAttr(seg.tenantId)}"`;
                if (seg.displayName) result += ` name="${escapeAttr(seg.displayName)}"`;
                result += '/>';
                break;
            case 'image':
                result += `<image src="${escapeAttr(seg.src)}"/>`;
                break;
            case 'link':
                result += '<link';
                result += ` href="${escapeAttr(seg.href)}"`;
                if (seg.title) result += ` title="${escapeAttr(seg.title)}"`;
                result += '/>';
                break;
            case 'br':
                result += '<br/>';
                break;
        }
    }
    return result;
}

// --- Internal helpers ---

function buildSegment(tag: string, attrs: Record<string, string>): MessageSegmentNode | null {
    switch (tag) {
        case 'at':
            return {
                type: 'at',
                userId: attrs['user'] || undefined,
                tenantId: attrs['tenant'] || undefined,
                displayName: attrs['name'] || undefined,
            };
        case 'image': {
            const src = attrs['src'];
            return src ? {type: 'image', src} : null;
        }
        case 'link': {
            const href = attrs['href'];
            return href ? {type: 'link', href, title: attrs['title'] || undefined} : null;
        }
        case 'br':
            return {type: 'br'};
        default:
            return null;
    }
}

function parseAttrs(raw: string): Record<string, string> {
    const trimmed = raw.trim().replace(/\/\s*$/, '').trim();
    if (!trimmed) return {};

    const result: Record<string, string> = {};
    let i = 0;

    while (i < trimmed.length) {
        while (i < trimmed.length && /\s/.test(trimmed[i])) i++;
        if (i >= trimmed.length) break;

        const nameStart = i;
        while (i < trimmed.length && trimmed[i] !== '=' && !/\s/.test(trimmed[i])) i++;
        const name = trimmed.substring(nameStart, i);
        if (i >= trimmed.length || trimmed[i] !== '=') {
            if (name) result[name] = '';
            continue;
        }
        i++; // skip '='

        if (i >= trimmed.length) break;
        const quote = trimmed[i];
        if (quote !== '"' && quote !== "'") {
            const valStart = i;
            while (i < trimmed.length && !/\s/.test(trimmed[i])) i++;
            result[name] = decodeAttr(trimmed.substring(valStart, i));
        } else {
            i++; // skip opening quote
            const valStart = i;
            while (i < trimmed.length && trimmed[i] !== quote) i++;
            result[name] = decodeAttr(trimmed.substring(valStart, i));
            if (i < trimmed.length) i++; // skip closing quote
        }
    }
    return result;
}

function decodeAttr(value: string): string {
    return decodeEntities(value);
}

function decodeEntities(s: string): string {
    if (!s.includes('&')) return s;
    return s
        .replace(/&amp;/g, '&')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&quot;/g, '"')
        .replace(/&apos;/g, "'");
}

function escapeText(text: string): string {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function escapeAttr(value: string): string {
    return value
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
