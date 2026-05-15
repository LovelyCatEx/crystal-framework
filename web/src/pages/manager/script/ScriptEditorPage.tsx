import {useCallback, useEffect, useRef, useState} from "react";
import {Editor, type Monaco, type OnMount} from "@monaco-editor/react";
import {Button, Card, Space, Tag} from "antd";
import {CheckCircleOutlined, CloseCircleOutlined, LinkOutlined, LoadingOutlined} from "@ant-design/icons";
import {doPost} from "@/api/system-request.ts";
import {LspClient, type LspCompletionItem, type LspDiagnosticsParams} from "./lsp-client.ts";

interface CompileDiagnostic {
    severity: string;
    message: string;
    line: number | null;
    column: number | null;
    lineEnd: number | null;
    columnEnd: number | null;
}

interface CompileResult {
    success: boolean;
    diagnostics: CompileDiagnostic[];
}

const DEFAULT_CODE = `fun main() {
    println("Hello, Crystal Framework!")
}
`;

export function ScriptEditorPage() {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const editorRef = useRef<any>(null);
    const monacoRef = useRef<Monaco | null>(null);
    const lspClientRef = useRef<LspClient | null>(null);
    const [compiling, setCompiling] = useState(false);
    const [lastResult, setLastResult] = useState<CompileResult | null>(null);
    const [lspConnected, setLspConnected] = useState(false);
    const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
    const documentOpened = useRef(false);

    // Connect to LSP on mount
    useEffect(() => {
        const client = new LspClient();
        lspClientRef.current = client;

        const wsUrl = `ws://${window.location.hostname}:${window.location.port || '8080'}/ws/lsp/kotlin`;

        client.connect(wsUrl).then(() => {
            setLspConnected(true);
            // Open the document
            client.openDocument(DEFAULT_CODE);
            documentOpened.current = true;
        }).catch(() => {
            setLspConnected(false);
        });

        // Handle diagnostics from LSP
        client.setOnDiagnostics((params: LspDiagnosticsParams) => {
            if (monacoRef.current && editorRef.current) {
                const model = editorRef.current.getModel();
                if (model) {
                    const markers = params.diagnostics.map((d) => ({
                        severity: d.severity === 1
                            ? monacoRef.current!.MarkerSeverity.Error
                            : d.severity === 2
                                ? monacoRef.current!.MarkerSeverity.Warning
                                : monacoRef.current!.MarkerSeverity.Info,
                        message: d.message,
                        startLineNumber: d.range.start.line + 1,
                        startColumn: d.range.start.character + 1,
                        endLineNumber: d.range.end.line + 1,
                        endColumn: d.range.end.character + 1,
                    }));
                    monacoRef.current!.editor.setModelMarkers(model, 'kotlin-lsp', markers);
                }
            }
        });

        return () => {
            client.disconnect();
        };
    }, []);

    const handleEditorMount: OnMount = (editorInstance, monaco) => {
        editorRef.current = editorInstance;
        monacoRef.current = monaco;

        // Ctrl+S → compile check
        editorInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
            const code = editorInstance.getValue();
            if (code) {
                void checkCompilation(code);
            }
        });

        // Register LSP-backed completion provider
        monaco.languages.registerCompletionItemProvider('kotlin', {
            triggerCharacters: ['.', ' ', '(', '[', ']', '{', '=', ',', '<', '>', ':', '"', "'", 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
            provideCompletionItems: async (model: unknown, position: unknown) => {
                const p = position as { lineNumber: number; column: number };
                const m = model as { getWordUntilPosition: (pos: unknown) => { startColumn: number; endColumn: number } };
                const word = m.getWordUntilPosition(position);
                const range = {
                    startLineNumber: p.lineNumber,
                    endLineNumber: p.lineNumber,
                    startColumn: word.startColumn,
                    endColumn: word.endColumn,
                };

                const client = lspClientRef.current;
                if (!client?.isConnected()) {
                    return { suggestions: getStaticSuggestions(monaco, range) };
                }

                const items = await client.getCompletionsDebounced(p.lineNumber, p.column, 100);

                if (items.length === 0) {
                    return { suggestions: getStaticSuggestions(monaco, range) };
                }

                const sortedItems = items.sort((a, b) => {
                    const priority = (kind: number | undefined): number => {
                        switch (kind) {
                            case 6: return 1; // Variable
                            case 3: return 2; // Function
                            case 12: return 3; // Property
                            case 8: return 4; // Method
                            case 4: return 5; // Constructor
                            case 5: return 6; // Field
                            case 2: return 7; // Constant
                            default: return 10; // Other
                        }
                    };
                    return priority(a.kind) - priority(b.kind);
                });

                const suggestions = sortedItems.map((item: LspCompletionItem) => ({
                    label: item.label,
                    kind: mapCompletionKind(monaco, item.kind),
                    insertText: item.insertText ?? item.label,
                    insertTextRules: item.insertTextFormat === 2
                        ? monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                        : undefined,
                    detail: item.detail,
                    range,
                }));

                return { suggestions };
            }
        });
    };

    const checkCompilation = useCallback(async (code: string) => {
        if (!code.trim()) return;

        setCompiling(true);
        try {
            const response = await doPost<CompileResult>('/api/script/compile-check', {sourceCode: code}, {'Content-Type': 'application/json'});
            const result = response.data!;
            setLastResult(result);

            // If LSP is not connected, use compile-check results for markers
            if (!lspClientRef.current?.isConnected() && monacoRef.current && editorRef.current) {
                const model = editorRef.current.getModel();
                if (model) {
                    const markers = result.diagnostics.map((d) => ({
                        severity: d.severity === 'ERROR'
                            ? monacoRef.current!.MarkerSeverity.Error
                            : monacoRef.current!.MarkerSeverity.Warning,
                        message: d.message,
                        startLineNumber: d.line ?? 1,
                        startColumn: d.column ?? 1,
                        endLineNumber: d.lineEnd ?? d.line ?? 1,
                        endColumn: d.columnEnd ?? (d.column ? d.column + 1 : 2),
                    }));
                    monacoRef.current.editor.setModelMarkers(model, 'kotlin-compile', markers);
                }
            }
        } catch {
            // ignore
        } finally {
            setCompiling(false);
        }
    }, []);

    const handleEditorChange = useCallback((value: string | undefined) => {
        // Update LSP document
        if (value && lspClientRef.current?.isConnected() && documentOpened.current) {
            lspClientRef.current.updateDocument(value);
        }

        // Debounce compile check
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }
        debounceTimer.current = setTimeout(() => {
            if (value) {
                void checkCompilation(value);
            }
        }, 1500);
    }, [checkCompilation]);

    const handleManualCheck = () => {
        const code = editorRef.current?.getValue();
        if (code) {
            void checkCompilation(code);
        }
    };

    return (
        <div className="h-full flex flex-col">
            <div className="mb-4 flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold">Kotlin Script Editor</h1>
                    <p className="text-gray-500 mt-1">编写 Kotlin 代码，实时检查编译状态</p>
                </div>
                <Space>
                    <Tag
                        color={lspConnected ? 'green' : 'default'}
                        icon={<LinkOutlined />}
                    >
                        {lspConnected ? 'LSP 已连接' : 'LSP 未连接'}
                    </Tag>
                    {lastResult && (
                        <Tag
                            color={lastResult.success ? 'green' : 'red'}
                            icon={lastResult.success ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
                        >
                            {lastResult.success ? '编译通过' : `${lastResult.diagnostics.length} 个错误`}
                        </Tag>
                    )}
                    {compiling && <Tag icon={<LoadingOutlined />} color="blue">编译检查中...</Tag>}
                    <Button type="primary" onClick={handleManualCheck} loading={compiling}>
                        检查编译
                    </Button>
                </Space>
            </div>

            <Card className="flex-1 border-none shadow-sm rounded-2xl overflow-hidden" styles={{body: {padding: 0, height: '100%'}}}>
                <Editor
                    height="calc(100vh - 200px)"
                    defaultLanguage="kotlin"
                    defaultValue={DEFAULT_CODE}
                    theme="vs-dark"
                    onChange={handleEditorChange}
                    onMount={handleEditorMount}
                    options={{
                        fontSize: 14,
                        minimap: {enabled: true},
                        scrollBeyondLastLine: false,
                        automaticLayout: true,
                        tabSize: 4,
                        wordWrap: 'on',
                        quickSuggestions: true,
                        suggestOnTriggerCharacters: true,
                    }}
                />
            </Card>
        </div>
    );
}

function mapCompletionKind(monaco: Monaco, kind?: number) {
    // LSP CompletionItemKind → Monaco CompletionItemKind
    switch (kind) {
        case 1: return monaco.languages.CompletionItemKind.Text;
        case 2: return monaco.languages.CompletionItemKind.Method;
        case 3: return monaco.languages.CompletionItemKind.Function;
        case 4: return monaco.languages.CompletionItemKind.Constructor;
        case 5: return monaco.languages.CompletionItemKind.Field;
        case 6: return monaco.languages.CompletionItemKind.Variable;
        case 7: return monaco.languages.CompletionItemKind.Class;
        case 8: return monaco.languages.CompletionItemKind.Interface;
        case 9: return monaco.languages.CompletionItemKind.Module;
        case 10: return monaco.languages.CompletionItemKind.Property;
        case 13: return monaco.languages.CompletionItemKind.Enum;
        case 14: return monaco.languages.CompletionItemKind.Keyword;
        case 15: return monaco.languages.CompletionItemKind.Snippet;
        default: return monaco.languages.CompletionItemKind.Text;
    }
}

function getStaticSuggestions(monaco: Monaco, range: { startLineNumber: number; endLineNumber: number; startColumn: number; endColumn: number }) {
    const keywords = [
        'fun', 'val', 'var', 'class', 'object', 'interface', 'abstract', 'open',
        'override', 'private', 'protected', 'public', 'internal',
        'if', 'else', 'when', 'for', 'while', 'do', 'return', 'break', 'continue',
        'try', 'catch', 'finally', 'throw', 'import', 'package',
        'data', 'sealed', 'enum', 'companion', 'suspend', 'inline',
        'null', 'true', 'false', 'this', 'super', 'is', 'as', 'in',
    ];

    const sdkSnippets = [
        {
            label: 'CrystalFrameworkScript',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
                'import com.lovelycatv.crystalframework.script.sdk.CrystalFrameworkScript',
                'import com.lovelycatv.crystalframework.script.sdk.ScriptMetadata',
                '',
                'object ${1:MyScript} : CrystalFrameworkScript() {',
                '    override val metadata = ScriptMetadata(',
                '        scriptName = "${2:My Script}",',
                '        scriptAuthor = "${3:Author}"',
                '    )',
                '',
                '    private val logger = Logger(metadata.scriptName)',
                '',
                '    override suspend fun execute() {',
                '        // Your script logic here',
                '        $0',
                '    }',
                '}'
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: 'Crystal Framework Script 模板',
            documentation: '创建一个继承 CrystalFrameworkScript 的脚本类',
        },
        {
            label: 'ScriptMetadata',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: 'ScriptMetadata(\n    scriptName = "${1:Script Name}",\n    scriptAuthor = "${2:Author}"\n)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: 'ScriptMetadata 数据类',
            documentation: '脚本元数据：包含脚本名称和作者信息',
        },
        {
            label: 'import-sdk',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: 'import com.lovelycatv.crystalframework.script.sdk.CrystalFrameworkScript\nimport com.lovelycatv.crystalframework.script.sdk.ScriptMetadata',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: '导入 SDK 类',
            documentation: '导入 CrystalFrameworkScript 和 ScriptMetadata',
        },
    ];

    const keywordSuggestions = keywords.map((kw) => ({
        label: kw,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: kw,
        range,
    }));

    return [...sdkSnippets, ...keywordSuggestions];
}
