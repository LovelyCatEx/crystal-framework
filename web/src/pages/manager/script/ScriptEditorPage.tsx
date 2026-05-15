import {useCallback, useRef, useState} from "react";
import CodeMirror from "@uiw/react-codemirror";
import {java} from "@codemirror/lang-java";
import {oneDark} from "@codemirror/theme-one-dark";
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
    const [code, setCode] = useState(DEFAULT_CODE);
    const [compiling, setCompiling] = useState(false);
    const [lastResult, setLastResult] = useState<CompileResult | null>(null);
    const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    const checkCompilation = useCallback(async (sourceCode: string) => {
        if (!sourceCode.trim()) return;

        setCompiling(true);
        try {
            const response = await doPost<CompileResult>('/api/script/compile-check', {sourceCode}, {'Content-Type': 'application/json'});
            const result = response.data!;
            setLastResult(result);
        } catch {
            // ignore
        } finally {
            setCompiling(false);
        }
    }, []);

    const handleChange = useCallback((value: string) => {
        setCode(value);
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }
        debounceTimer.current = setTimeout(() => {
            void checkCompilation(value);
        }, 1500);
    }, [checkCompilation]);

    const handleManualCheck = () => {
        void checkCompilation(code);
    };

    return (
        <div className="h-full flex flex-col">
            <div className="mb-4 flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold">Kotlin Script Editor</h1>
                    <p className="text-gray-500 mt-1">编写 Kotlin 代码，实时检查编译状态</p>
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
                <CodeMirror
                    value={code}
                    height="calc(100vh - 200px)"
                    theme={oneDark}
                    extensions={[java()]}
                    onChange={handleChange}
                    basicSetup={{
                        lineNumbers: true,
                        highlightActiveLineGutter: true,
                        highlightActiveLine: true,
                        foldGutter: true,
                        autocompletion: true,
                        bracketMatching: true,
                        closeBrackets: true,
                        indentOnInput: true,
                    }}
                />
            </Card>

            {lastResult && !lastResult.success && (
                <Card className="mt-4 border-none shadow-sm rounded-2xl" styles={{body: {padding: '12px 16px'}}}>
                    <div className="text-sm font-bold text-red-500 mb-2">编译错误：</div>
                    {lastResult.diagnostics.map((d, i) => (
                        <div key={i} className="text-xs font-mono text-red-400 mb-1">
                            {d.line && d.column ? `[${d.line}:${d.column}] ` : ''}{d.message}
                        </div>
                    ))}
                </Card>
            )}
        </div>
    );
}
