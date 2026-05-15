import {useCallback, useRef, useState} from "react";
import {Editor, type Monaco, type OnMount} from "@monaco-editor/react";
import {Button, Card, Space, Tag} from "antd";
import {CheckCircleOutlined, CloseCircleOutlined, LoadingOutlined} from "@ant-design/icons";
import {doPost} from "@/api/system-request.ts";

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
    const [compiling, setCompiling] = useState(false);
    const [lastResult, setLastResult] = useState<CompileResult | null>(null);
    const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    const handleEditorMount: OnMount = (editorInstance, monaco) => {
        editorRef.current = editorInstance;
        monacoRef.current = monaco;

        // 注册 Kotlin 代码提示
        monaco.languages.registerCompletionItemProvider('kotlin', {
            triggerCharacters: ['.', ' '],
            provideCompletionItems: (model: unknown, position: unknown) => {
                const m = model as { getWordUntilPosition: (pos: unknown) => { startColumn: number; endColumn: number } };
                const p = position as { lineNumber: number; column: number };
                const word = m.getWordUntilPosition(position);
                const range = {
                    startLineNumber: p.lineNumber,
                    endLineNumber: p.lineNumber,
                    startColumn: word.startColumn,
                    endColumn: word.endColumn,
                };

                const keywords = [
                    'fun', 'val', 'var', 'class', 'object', 'interface', 'abstract', 'open',
                    'override', 'private', 'protected', 'public', 'internal',
                    'if', 'else', 'when', 'for', 'while', 'do', 'return', 'break', 'continue',
                    'try', 'catch', 'finally', 'throw', 'import', 'package',
                    'data', 'sealed', 'enum', 'companion', 'suspend', 'inline', 'crossinline',
                    'noinline', 'reified', 'typealias', 'lateinit', 'by', 'lazy',
                    'null', 'true', 'false', 'this', 'super', 'is', 'as', 'in', 'out',
                ];

                const builtins = [
                    { label: 'println', insertText: 'println(${1})', detail: 'fun println(message: Any?)' },
                    { label: 'print', insertText: 'print(${1})', detail: 'fun print(message: Any?)' },
                    { label: 'listOf', insertText: 'listOf(${1})', detail: 'fun <T> listOf(vararg elements: T): List<T>' },
                    { label: 'mutableListOf', insertText: 'mutableListOf(${1})', detail: 'fun <T> mutableListOf(vararg elements: T): MutableList<T>' },
                    { label: 'mapOf', insertText: 'mapOf(${1})', detail: 'fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V>' },
                    { label: 'mutableMapOf', insertText: 'mutableMapOf(${1})', detail: 'fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V>' },
                    { label: 'setOf', insertText: 'setOf(${1})', detail: 'fun <T> setOf(vararg elements: T): Set<T>' },
                    { label: 'arrayOf', insertText: 'arrayOf(${1})', detail: 'fun <T> arrayOf(vararg elements: T): Array<T>' },
                    { label: 'require', insertText: 'require(${1})', detail: 'fun require(value: Boolean)' },
                    { label: 'check', insertText: 'check(${1})', detail: 'fun check(value: Boolean)' },
                    { label: 'repeat', insertText: 'repeat(${1:times}) {\n\t${2}\n}', detail: 'fun repeat(times: Int, action: (Int) -> Unit)' },
                    { label: 'TODO', insertText: 'TODO("${1}")', detail: 'fun TODO(reason: String): Nothing' },
                    { label: 'run', insertText: 'run {\n\t${1}\n}', detail: 'fun <R> run(block: () -> R): R' },
                    { label: 'let', insertText: 'let { ${1:it} ->\n\t${2}\n}', detail: 'fun <T, R> T.let(block: (T) -> R): R' },
                    { label: 'also', insertText: 'also { ${1:it} ->\n\t${2}\n}', detail: 'fun <T> T.also(block: (T) -> Unit): T' },
                    { label: 'apply', insertText: 'apply {\n\t${1}\n}', detail: 'fun <T> T.apply(block: T.() -> Unit): T' },
                    { label: 'with', insertText: 'with(${1}) {\n\t${2}\n}', detail: 'fun <T, R> with(receiver: T, block: T.() -> R): R' },
                    { label: 'String', insertText: 'String', detail: 'class String' },
                    { label: 'Int', insertText: 'Int', detail: 'class Int' },
                    { label: 'Long', insertText: 'Long', detail: 'class Long' },
                    { label: 'Double', insertText: 'Double', detail: 'class Double' },
                    { label: 'Boolean', insertText: 'Boolean', detail: 'class Boolean' },
                    { label: 'List', insertText: 'List<${1}>', detail: 'interface List<out E>' },
                    { label: 'Map', insertText: 'Map<${1}, ${2}>', detail: 'interface Map<K, out V>' },
                    { label: 'Set', insertText: 'Set<${1}>', detail: 'interface Set<out E>' },
                ];

                const suggestions = [
                    ...keywords.map((kw) => ({
                        label: kw,
                        kind: monaco.languages.CompletionItemKind.Keyword,
                        insertText: kw,
                        range,
                    })),
                    ...builtins.map((b) => ({
                        label: b.label,
                        kind: monaco.languages.CompletionItemKind.Function,
                        insertText: b.insertText,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        detail: b.detail,
                        range,
                    })),
                ];

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

            if (monacoRef.current && editorRef.current) {
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
                </div>
                <Space>
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
                    }}
                />
            </Card>
        </div>
    );
}
