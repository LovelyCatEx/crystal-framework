import {useEffect, useMemo, useRef, useState} from "react";
import {Button, Card, Empty, Input, message, Select, Spin, Switch, Typography} from "antd";
import {BulbOutlined, DownOutlined, SendOutlined, ToolOutlined} from "@ant-design/icons";
import type {DataNode} from "antd/es/tree";
import {useTranslation} from "react-i18next";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import {chat, chatStream, type AiPlaygroundChatDTO, type AiPlaygroundMessage} from "@/api/ai/ai-playground.api.ts";
import {ReasoningEffort, type AiModelEntity, type AiProviderEntity} from "@/types/ai/ai.types.ts";
import {getReasoningEffort} from "@/i18n/enum-helpers.ts";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TreeDetailLayout} from "@/components/layouts/TreeDetailLayout.tsx";

const {TextArea} = Input;
const {Text} = Typography;

/** Select value meaning "send nothing and let the provider's protocol choose". */
const REASONING_EFFORT_PROTOCOL_DEFAULT = "";

export default function AiPlaygroundPage() {
    const {t} = useTranslation();
    const [providers, setProviders] = useState<AiProviderEntity[]>([]);
    const [models, setModels] = useState<AiModelEntity[]>([]);
    const [selectedModelId, setSelectedModelId] = useState<string | null>(null);
    const [messageList, setMessageList] = useState<AiPlaygroundMessage[]>([]);
    const [expandedThinking, setExpandedThinking] = useState<Set<number>>(new Set());
    const [expandedToolCalls, setExpandedToolCalls] = useState<Set<string>>(new Set());
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [reasoningEffort, setReasoningEffort] = useState<ReasoningEffort | undefined>(undefined);
    const [streaming, setStreaming] = useState(true);
    const messageListRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        let active = true;
        setLoading(true);
        Promise.all([
            AiProviderManagerController.readAll(),
            AiModelManagerController.readAll(),
        ]).then(([providerResponse, modelResponse]) => {
            if (!active) return;
            setProviders(providerResponse.data ?? []);
            setModels(modelResponse.data ?? []);
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

    const treeData = useMemo<DataNode[]>(() => providers.map(provider => ({
        key: `provider:${provider.id}`,
        title: provider.name,
        selectable: false,
        children: models
            .filter(model => model.providerId === provider.id && model.enabled)
            .map(model => ({
                key: `model:${model.id}`,
                title: model.displayName,
                isLeaf: true,
            })),
    })), [models, providers]);

    const selectedModel = models.find(model => model.id === selectedModelId) ?? null;

    const reasoningEffortOptions = useMemo(() => [
        {value: REASONING_EFFORT_PROTOCOL_DEFAULT, label: t("pages.aiPlayground.reasoningEffortDefault")},
        ...Object.values(ReasoningEffort).map(effort => ({
            value: effort,
            label: getReasoningEffort(effort),
        })),
    ], [t]);

    const selectModel = (key: string | null) => {
        if (!key?.startsWith("model:")) return;
        const modelId = key.substring("model:".length);
        setSelectedModelId(modelId);
        setMessageList([]);
        setExpandedThinking(new Set());
        setInput("");
    };

    const sendMessage = async () => {
        const content = input.trim();
        if (!selectedModelId || !content || sending) return;
        const userMessage: AiPlaygroundMessage = {role: "user", content};
        const nextMessages = [...messageList, userMessage];
        setMessageList(nextMessages);
        setInput("");
        setSending(true);
        const payload: AiPlaygroundChatDTO = {
            modelId: selectedModelId,
            messages: nextMessages.map(({role, content}) => ({role, content})),
            reasoningEffort,
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
        <div className="flex h-full flex-col">
            <ActionBarComponent
                title={t("pages.aiPlayground.title")}
                subtitle={t("pages.aiPlayground.subtitle")}
            />
            <TreeDetailLayout
                tree={{
                    title: t("pages.aiPlayground.modelTree"),
                    treeData,
                    selectedKey: selectedModelId ? `model:${selectedModelId}` : null,
                    loading,
                    onSelect: selectModel,
                    emptyContent: <Empty description={t("pages.aiPlayground.emptyModels")} />,
                }}
                detail={{
                    emptyContent: <Empty description={t("pages.aiPlayground.selectModelHint")} />,
                    content: (
                        <Card
                            title={selectedModel?.displayName}
                            className="flex h-full min-h-96 flex-col border-none shadow-sm rounded-2xl overflow-hidden"
                            styles={{body: {display: "flex", flex: 1, flexDirection: "column", minHeight: 0}}}
                        >
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
                            <div className="border-t p-3">
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
                        </Card>
                    ),
                }}
            />
        </div>
    );
}
