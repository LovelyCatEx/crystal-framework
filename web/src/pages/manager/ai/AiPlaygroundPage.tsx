import {useEffect, useMemo, useRef, useState} from "react";
import {Button, Card, Descriptions, Empty, Input, message, Popconfirm, Select, Spin, Switch, Tag, Tree, Typography} from "antd";
import {BulbOutlined, DownOutlined, PlusOutlined, SendOutlined, ToolOutlined} from "@ant-design/icons";
import type {DataNode} from "antd/es/tree";
import {useTranslation} from "react-i18next";
import {chat, chatStream, getPlaygroundData, type AiPlaygroundChatDTO, type AiPlaygroundGroup, type AiPlaygroundMessage, type AiPlaygroundModel, type AiPlaygroundProvider} from "@/api/ai/ai-playground.api.ts";
import {ReasoningEffort} from "@/types/ai/ai.types.ts";
import {getAiModelCapability, getReasoningEffort} from "@/i18n/enum-helpers.ts";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";

const {TextArea} = Input;
const {Text} = Typography;

/** Select value meaning "send nothing and let the provider's protocol choose". */
const REASONING_EFFORT_PROTOCOL_DEFAULT = "";

const SESSION_STORAGE_KEY_PREFIX = "ai_playground:";

interface StoredSession {
    sessionId: string;
    messages: AiPlaygroundMessage[];
}

function loadStoredSession(modelId: string): StoredSession | null {
    try {
        const raw = localStorage.getItem(`${SESSION_STORAGE_KEY_PREFIX}${modelId}`);
        if (!raw) return null;
        const parsed = JSON.parse(raw) as StoredSession;
        if (parsed && typeof parsed.sessionId === "string" && Array.isArray(parsed.messages)) {
            return parsed;
        }
    } catch {
        // Ignore malformed entries.
    }
    return null;
}

function saveStoredSession(modelId: string, sessionId: string, messages: AiPlaygroundMessage[]): void {
    try {
        localStorage.setItem(
            `${SESSION_STORAGE_KEY_PREFIX}${modelId}`,
            JSON.stringify({sessionId, messages}),
        );
    } catch {
        // Ignore quota / serialization errors.
    }
}

/** Formats a billing multiplier as "1x", "1.5x", "1.25x" — at most two decimals, trailing zeros dropped. */
function formatMultiplier(value: string): string {
    const num = Number(value);
    if (!Number.isFinite(num)) return value;
    return `${num.toFixed(2).replace(/\.?0+$/, "")}x`;
}

/** Formats a price as "2.00 CNY/M" — always two decimals, currency appended. */
function formatPrice(value: string | null, currency: string): string {
    if (value == null) return "-";
    const num = Number(value);
    return `${Number.isFinite(num) ? num.toFixed(2) : value} ${currency}/M`;
}

export default function AiPlaygroundPage() {
    const {t} = useTranslation();
    const [providers, setProviders] = useState<Record<string, AiPlaygroundProvider>>({});
    const [models, setModels] = useState<Record<string, AiPlaygroundModel>>({});
    const [selectedModelId, setSelectedModelId] = useState<string | null>(null);
    const [messageList, setMessageList] = useState<AiPlaygroundMessage[]>([]);
    const [expandedThinking, setExpandedThinking] = useState<Set<number>>(new Set());
    const [expandedToolCalls, setExpandedToolCalls] = useState<Set<string>>(new Set());
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [reasoningEffort, setReasoningEffort] = useState<ReasoningEffort | undefined>(undefined);
    const [streaming, setStreaming] = useState(true);
    const [sessionId, setSessionId] = useState<string | null>(null);
    const [groups, setGroups] = useState<Record<string, AiPlaygroundGroup>>({});
    const [groupId, setGroupId] = useState<string | null>(null);
    const messageListRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        let active = true;
        setLoading(true);
        getPlaygroundData().then(res => {
            if (!active) return;
            if (res.data) {
                setProviders(res.data.providers);
                setGroups(res.data.groups);
                setModels(res.data.models);
            }
        }).catch(() => {
            if (active) {
                void message.error(t("pages.aiPlayground.messages.loadFailed"));
            }
        }).finally(() => {
            if (active) setLoading(false);
        });
        return () => {
            active = false;
        };
    }, [t]);

    useEffect(() => {
        messageListRef.current?.scrollTo({top: messageListRef.current.scrollHeight});
    }, [messageList]);

    useEffect(() => {
        if (selectedModelId && sessionId) {
            saveStoredSession(selectedModelId, sessionId, messageList);
        }
    }, [selectedModelId, sessionId, messageList]);

    const groupCountByModel = useMemo(() => {
        const counts: Record<string, number> = {};
        for (const group of Object.values(groups)) {
            for (const modelId of group.modelIds) {
                counts[modelId] = (counts[modelId] ?? 0) + 1;
            }
        }
        return counts;
    }, [groups]);

    const treeData = useMemo<DataNode[]>(() => Object.entries(providers).map(([providerId, provider]) => ({
        key: `provider:${providerId}`,
        title: provider.name,
        selectable: false,
        children: provider.modelIds
            .filter(modelId => models[modelId] != null)
            .map(modelId => ({
                key: `model:${modelId}`,
                title: `${models[modelId].displayName} (${groupCountByModel[modelId] ?? 0})`,
                isLeaf: true,
            })),
    })), [models, providers, groupCountByModel]);

    const selectedModel = selectedModelId ? (models[selectedModelId] ?? null) : null;

    const modelInfoCard = useMemo(() => {
        if (!selectedModel) return null;
        return (
            <div className="shrink-0 border-t p-4">
                <Text strong className="mb-2 block">{t("pages.aiPlayground.modelInfo")}</Text>
                <Descriptions column={1} size="small" colon={false}>
                    <Descriptions.Item label={t("pages.aiPlayground.modelKey")}>{selectedModel.key}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelInputPrice")}>{formatPrice(selectedModel.inputPricePerMillion, selectedModel.currency)}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelOutputPrice")}>{formatPrice(selectedModel.outputPricePerMillion, selectedModel.currency)}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelCacheReadPrice")}>{formatPrice(selectedModel.cacheReadPricePerMillion, selectedModel.currency)}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelCacheWritePrice")}>{formatPrice(selectedModel.cacheWritePricePerMillion, selectedModel.currency)}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelContextWindow")}>{selectedModel.contextWindowTokens}</Descriptions.Item>
                    <Descriptions.Item label={t("pages.aiPlayground.modelCapabilities")}>
                        <div className="flex flex-wrap gap-1">
                            {selectedModel.capabilities.map(cap => (
                                <Tag key={cap}>{getAiModelCapability(cap)}</Tag>
                            ))}
                        </div>
                    </Descriptions.Item>
                </Descriptions>
            </div>
        );
    }, [selectedModel, t]);

    const reasoningEffortOptions = useMemo(() => [
        {value: REASONING_EFFORT_PROTOCOL_DEFAULT, label: t("pages.aiPlayground.reasoningEffortDefault")},
        ...Object.values(ReasoningEffort).map(effort => ({
            value: effort,
            label: getReasoningEffort(effort),
        })),
    ], [t]);

    const userGroupOptions = useMemo(() => {
        const options: {value: string; label: string}[] = [];
        if (selectedModelId) {
            for (const [groupId, group] of Object.entries(groups)) {
                if (group.modelIds.includes(selectedModelId)) {
                    options.push({
                        value: groupId,
                        label: `${group.name} ${formatMultiplier(group.billingMultiplier)}`,
                    });
                }
            }
        }
        return options;
    }, [groups, selectedModelId, t]);

    const selectModel = (key: string | null) => {
        if (!key?.startsWith("model:")) return;
        const modelId = key.substring("model:".length);
        setSelectedModelId(modelId);
        setExpandedThinking(new Set());
        setInput("");
        const stored = loadStoredSession(modelId);
        if (stored) {
            setSessionId(stored.sessionId);
            setMessageList(stored.messages);
        } else {
            setSessionId(crypto.randomUUID());
            setMessageList([]);
        }
        setGroupId(
            Object.entries(groups).find(([, group]) => group.modelIds.includes(modelId))?.[0] ?? null
        );
    };

    const startNewSession = () => {
        setMessageList([]);
        setExpandedThinking(new Set());
        setExpandedToolCalls(new Set());
        setInput("");
        setSessionId(crypto.randomUUID());
    };

    const sendMessage = async () => {
        const content = input.trim();
        if (!selectedModelId || !content || sending) return;
        if (!groupId) {
            void message.error(t("pages.aiPlayground.messages.groupRequired"));
            return;
        }
        const currentSessionId = sessionId ?? crypto.randomUUID();
        setSessionId(currentSessionId);
        const userMessage: AiPlaygroundMessage = {role: "user", content};
        const nextMessages = [...messageList, userMessage];
        setMessageList(nextMessages);
        setInput("");
        setSending(true);
        const payload: AiPlaygroundChatDTO = {
            modelId: selectedModelId,
            messages: nextMessages.map(({role, content}) => ({role, content})),
            reasoningEffort,
            sessionId: currentSessionId,
            groupId,
        };
        try {
            if (streaming) {
                setMessageList([...nextMessages, {role: "assistant", content: "", reasoningContent: ""}]);
                try {
                    await chatStream(payload, chunk => {
                        setMessageList(prev => {
                            const next = [...prev];
                            const last = next[next.length - 1];
                            next[next.length - 1] = {
                                ...last,
                                content: (last.content || "") + (chunk.content || ""),
                                reasoningContent: (last.reasoningContent || "") + (chunk.reasoningContent || ""),
                                usage: chunk.usage ?? last.usage,
                                toolCalls: chunk.toolCall ? [...(last.toolCalls || []), chunk.toolCall] : last.toolCalls,
                            };
                            return next;
                        });
                    });
                } catch (streamError) {
                    // Roll back the placeholder assistant message so a failed stream does not leave
                    // an empty-content message behind — it would fail the next request's @NotBlank.
                    setMessageList(nextMessages);
                    throw streamError;
                }
            } else {
                const response = await chat(payload);
                if (response.data) {
                    setMessageList(prev => [...prev, {
                        role: "assistant",
                        content: response.data!.content,
                        reasoningContent: response.data!.reasoningContent,
                        usage: response.data!.usage ?? undefined,
                        toolCalls: response.data!.toolCalls ?? undefined,
                    }]);
                }
            }
        } catch (error) {
            void message.error(
                error instanceof Error && error.message
                    ? error.message
                    : t("pages.aiPlayground.messages.chatFailed")
            );
        } finally {
            setSending(false);
        }
    };

    const toggleThinking = (index: number) => {
        setExpandedThinking(prev => {
            const next = new Set(prev);
            if (next.has(index)) {
                next.delete(index);
            } else {
                next.add(index);
            }
            return next;
        });
    };

    const toggleToolCall = (key: string) => {
        setExpandedToolCalls(prev => {
            const next = new Set(prev);
            if (next.has(key)) {
                next.delete(key);
            } else {
                next.add(key);
            }
            return next;
        });
    };

    const isThinking = (index: number) => streaming && sending && index === messageList.length - 1;

    return (
        <div className="flex h-[calc(100vh-158px)] flex-col">
            <ActionBarComponent
                title={t("pages.aiPlayground.title")}
                subtitle={t("pages.aiPlayground.subtitle")}
            />
            <Card
                className="flex flex-1 min-h-0 flex-col border-none shadow-sm rounded-2xl overflow-hidden"
                styles={{body: {display: "flex", flex: 1, flexDirection: "column", minHeight: 0, padding: 0}}}
            >
                <div className="flex flex-1 min-h-0">
                    <div className="flex w-72 shrink-0 flex-col border-r" style={{borderColor: "var(--ant-color-border)"}}>
                        <div className="flex h-14 shrink-0 items-center border-b px-4">
                            <Text strong>{t("pages.aiPlayground.modelTree")}</Text>
                        </div>
                        <div className="flex-1 min-h-0 overflow-auto p-4">
                            {loading ? (
                                <Spin />
                            ) : treeData.length > 0 ? (
                                <Tree
                                    treeData={treeData}
                                    onSelect={keys => selectModel((keys[0] as string | undefined) ?? null)}
                                    selectedKeys={selectedModelId ? [`model:${selectedModelId}`] : []}
                                    defaultExpandAll
                                    blockNode
                                    showLine
                                />
                            ) : (
                                <Empty description={t("pages.aiPlayground.emptyModels")} />
                            )}
                        </div>
                        {modelInfoCard}
                    </div>
                    <div className="flex flex-1 flex-col">
                        <div className="flex h-14 shrink-0 items-center justify-between border-b px-4">
                            <Text strong>{selectedModel?.displayName}</Text>
                            <Popconfirm
                                title={t("pages.aiPlayground.newSessionConfirm")}
                                onConfirm={startNewSession}
                                okText={t("components.managerPageContainer.confirm")}
                                cancelText={t("components.managerPageContainer.cancel")}
                            >
                                <Button size="small" icon={<PlusOutlined />}>
                                    {t("pages.aiPlayground.newSession")}
                                </Button>
                            </Popconfirm>
                        </div>
                            <div ref={messageListRef} className="flex-1 overflow-auto p-4">
                                {messageList.length === 0 ? (
                                    <div className="flex h-full items-center justify-center">
                                        <Text type="secondary">{t("pages.aiPlayground.emptyConversation")}</Text>
                                    </div>
                                ) : messageList.map((messageItem, index) => (
                                    <div
                                        key={`${messageItem.role}-${index}`}
                                        className={`mb-3 flex ${messageItem.role === "user" ? "justify-end" : "justify-start"}`}
                                    >
                                        <div className="max-w-[75%] whitespace-pre-wrap rounded-xl px-3 py-2" style={{background: messageItem.role === "user" ? "var(--ant-color-primary)" : "var(--ant-color-fill-secondary)", color: messageItem.role === "user" ? "white" : "inherit"}}>
                                            {messageItem.reasoningContent ? (
                                                <>
                                                    <button
                                                        type="button"
                                                        onClick={() => toggleThinking(index)}
                                                        className="mb-1 flex items-center gap-1.5"
                                                        style={{color: "var(--ant-color-text-secondary)", cursor: "pointer", background: "transparent", border: "none", padding: 0}}
                                                    >
                                                        {isThinking(index) ? <Spin size="small" /> : <BulbOutlined />}
                                                        <span>{isThinking(index) ? t("pages.aiPlayground.thinking") : t("pages.aiPlayground.thoughtComplete")}</span>
                                                        <DownOutlined
                                                            style={{transition: "transform 0.2s", transform: expandedThinking.has(index) ? "rotate(180deg)" : "none"}}
                                                        />
                                                    </button>
                                                    {expandedThinking.has(index) && (
                                                        <div className="mb-2 border-l-2 pl-2" style={{color: "var(--ant-color-text-secondary)", borderColor: "var(--ant-color-border-secondary)"}}>
                                                            {messageItem.reasoningContent}
                                                        </div>
                                                    )}
                                                </>
                                            ) : null}
                                            {messageItem.toolCalls && messageItem.toolCalls.length > 0 ? (
                                                <div className="mb-2 flex flex-col gap-1">
                                                    {messageItem.toolCalls.map((toolCall, toolIndex) => {
                                                        const toolKey = `${index}-${toolIndex}`;
                                                        const toolExpanded = expandedToolCalls.has(toolKey);
                                                        const hasArguments = toolCall.arguments != null && Object.keys(toolCall.arguments).length > 0;
                                                        return (
                                                            <div key={toolIndex}>
                                                                <button
                                                                    type="button"
                                                                    onClick={() => hasArguments && toggleToolCall(toolKey)}
                                                                    className="flex items-center gap-1.5 text-xs"
                                                                    style={{color: "var(--ant-color-text-secondary)", cursor: hasArguments ? "pointer" : "default", background: "transparent", border: "none", padding: 0}}
                                                                >
                                                                    <ToolOutlined />
                                                                    <span className="font-medium">{toolCall.toolName}</span>
                                                                    {hasArguments ? <DownOutlined style={{transition: "transform 0.2s", transform: toolExpanded ? "rotate(180deg)" : "none"}} /> : null}
                                                                    <span>→</span>
                                                                    <span className="break-all">{toolCall.result}</span>
                                                                </button>
                                                                {toolExpanded && hasArguments ? (
                                                                    <div className="mt-1 flex gap-1.5 text-xs">
                                                                        <ToolOutlined style={{visibility: "hidden"}} />
                                                                        <pre className="m-0 flex-1 whitespace-pre-wrap" style={{color: "var(--ant-color-text-secondary)"}}>
                                                                            {JSON.stringify(toolCall.arguments, null, 2)}
                                                                        </pre>
                                                                    </div>
                                                                ) : null}
                                                            </div>
                                                        );
                                                    })}
                                                </div>
                                            ) : null}
                                            {messageItem.content}
                                            {messageItem.usage ? (
                                                <div className="mt-2 flex flex-wrap items-center gap-x-2 text-xs" style={{color: "var(--ant-color-text-tertiary)"}}>
                                                    <span>{t("pages.aiPlayground.usagePromptTokens")} {messageItem.usage.promptTokens}</span>
                                                    <span>·</span>
                                                    <span>{t("pages.aiPlayground.usageCompletionTokens")} {messageItem.usage.completionTokens}</span>
                                                    {messageItem.usage.reasoningTokens > 0 ? (
                                                        <>
                                                            <span>·</span>
                                                            <span>{t("pages.aiPlayground.usageReasoningTokens")} {messageItem.usage.reasoningTokens}</span>
                                                        </>
                                                    ) : null}
                                                    {messageItem.usage.cachedPromptTokens > 0 ? (
                                                        <>
                                                            <span>·</span>
                                                            <span>{t("pages.aiPlayground.usageCachedTokens")} {messageItem.usage.cachedPromptTokens}</span>
                                                        </>
                                                    ) : null}
                                                    {messageItem.usage.cacheCreationTokens > 0 ? (
                                                        <>
                                                            <span>·</span>
                                                            <span>{t("pages.aiPlayground.usageCacheCreationTokens")} {messageItem.usage.cacheCreationTokens}</span>
                                                        </>
                                                    ) : null}
                                                    {messageItem.toolCalls && messageItem.toolCalls.length > 0 ? (
                                                        <>
                                                            <span>·</span>
                                                            <span>{t("pages.aiPlayground.usageToolCalls")} {messageItem.toolCalls.length}</span>
                                                        </>
                                                    ) : null}
                                                </div>
                                            ) : null}
                                        </div>
                                    </div>
                                ))}
                                {sending && <Spin size="small" />}
                            </div>
                            <div className="shrink-0 border-t p-3">
                                <div className="rounded-2xl border p-2" style={{borderColor: "var(--ant-color-border)"}}>
                                    <TextArea
                                        variant="borderless"
                                        value={input}
                                        disabled={sending}
                                        autoSize={{minRows: 2, maxRows: 6}}
                                        placeholder={t("pages.aiPlayground.inputPlaceholder")}
                                        onChange={event => setInput(event.target.value)}
                                        onPressEnter={event => {
                                            if (!event.shiftKey) {
                                                event.preventDefault();
                                                void sendMessage();
                                            }
                                        }}
                                    />
                                    <div className="mt-1 flex items-center justify-between">
                                        <div className="flex items-center gap-3">
                                            <div className="flex items-center gap-2">
                                                <Text type="secondary">{t("pages.aiPlayground.streaming")}</Text>
                                                <Switch
                                                    size="small"
                                                    checked={streaming}
                                                    disabled={sending}
                                                    onChange={setStreaming}
                                                />
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Text type="secondary">{t("pages.aiPlayground.reasoningEffort")}</Text>
                                                <Select
                                                    size="small"
                                                    style={{width: 150}}
                                                    disabled={sending}
                                                    value={reasoningEffort ?? REASONING_EFFORT_PROTOCOL_DEFAULT}
                                                    options={reasoningEffortOptions}
                                                    onChange={value => setReasoningEffort(
                                                        value === REASONING_EFFORT_PROTOCOL_DEFAULT
                                                            ? undefined
                                                            : value as ReasoningEffort
                                                    )}
                                                />
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Text type="secondary">{t("pages.aiPlayground.group")}</Text>
                                                <Select
                                                    size="small"
                                                    style={{width: 150}}
                                                    disabled={sending}
                                                    value={groupId ?? ""}
                                                    options={userGroupOptions}
                                                    onChange={value => setGroupId(value === "" ? null : value)}
                                                />
                                            </div>
                                        </div>
                                        <Button
                                            type="primary"
                                            icon={<SendOutlined />}
                                            loading={sending}
                                            onClick={() => void sendMessage()}
                                        >
                                            {t("pages.aiPlayground.send")}
                                        </Button>
                                    </div>
                                </div>
                            </div>
                    </div>
                </div>
            </Card>
        </div>
    );
}
