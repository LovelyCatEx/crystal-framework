import {useEffect, useMemo, useRef, useState} from "react";
import {Button, Card, Empty, Input, message, Spin, Typography} from "antd";
import {BulbOutlined, DownOutlined, SendOutlined} from "@ant-design/icons";
import type {DataNode} from "antd/es/tree";
import {useTranslation} from "react-i18next";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import {chat, type AiPlaygroundMessage} from "@/api/ai/ai-playground.api.ts";
import type {AiModelEntity, AiProviderEntity} from "@/types/ai/ai.types.ts";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TreeDetailLayout} from "@/components/layouts/TreeDetailLayout.tsx";

const {TextArea} = Input;
const {Text} = Typography;

export default function AiPlaygroundPage() {
    const {t} = useTranslation();
    const [providers, setProviders] = useState<AiProviderEntity[]>([]);
    const [models, setModels] = useState<AiModelEntity[]>([]);
    const [selectedModelId, setSelectedModelId] = useState<string | null>(null);
    const [messageList, setMessageList] = useState<AiPlaygroundMessage[]>([]);
    const [expandedThinking, setExpandedThinking] = useState<Set<number>>(new Set());
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
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
        try {
            const response = await chat({
                modelId: selectedModelId,
                messages: nextMessages.map(({role, content}) => ({role, content})),
            });
            if (response.data) {
                setMessageList([...nextMessages, {
                    role: "assistant",
                    content: response.data.content,
                    reasoningContent: response.data.reasoningContent,
                }]);
            }
        } catch {
            void message.error(t("pages.aiPlayground.messages.chatFailed"));
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
                                                        <BulbOutlined />
                                                        <span>{t("pages.aiPlayground.thinking")}</span>
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
                                            {messageItem.content}
                                        </div>
                                    </div>
                                ))}
                                {sending && <Spin size="small" />}
                            </div>
                            <div className="flex gap-2 border-t p-3">
                                <TextArea
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
                                <Button
                                    type="primary"
                                    icon={<SendOutlined />}
                                    loading={sending}
                                    onClick={() => void sendMessage()}
                                >
                                    {t("pages.aiPlayground.send")}
                                </Button>
                            </div>
                        </Card>
                    ),
                }}
            />
        </div>
    );
}
