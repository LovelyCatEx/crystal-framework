import {useState, type ReactNode} from "react";
import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";
import remarkBreaks from "remark-breaks";
import {Prism as SyntaxHighlighter} from "react-syntax-highlighter";
import {oneDark} from "react-syntax-highlighter/dist/esm/styles/prism";
import {Button, theme} from "antd";
import {CheckOutlined, CopyOutlined} from "@ant-design/icons";

interface MarkdownContentProps {
    content: string;
}

interface CodeBlockProps {
    code: string;
    dark?: boolean;
    children: ReactNode;
}

function CodeBlock({code, dark, children}: CodeBlockProps) {
    const [copied, setCopied] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(code);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch {
            // Clipboard unavailable — ignore.
        }
    };

    return (
        <div className="relative my-2">
            {children}
            <Button
                size="small"
                type="text"
                className="absolute right-2 top-2"
                style={dark ? {color: "rgba(255,255,255,0.7)"} : undefined}
                icon={copied ? <CheckOutlined /> : <CopyOutlined />}
                onClick={() => void handleCopy()}
            />
        </div>
    );
}

/**
 * Renders markdown text (CommonMark + GitHub Flavored Markdown + single-newline line breaks) into
 * styled React elements, sized for use inside a chat bubble. Raw HTML is escaped by react-markdown by
 * default, so AI-generated content cannot inject markup.
 */
export function MarkdownContent({content}: MarkdownContentProps) {
    const {token} = theme.useToken();

    return (
        <Markdown
            remarkPlugins={[remarkGfm, remarkBreaks]}
            components={{
                h1: (props) => <h1 {...props} className="my-2 text-xl font-bold" />,
                h2: (props) => <h2 {...props} className="my-2 text-lg font-bold" />,
                h3: (props) => <h3 {...props} className="my-1.5 text-base font-semibold" />,
                h4: (props) => <h4 {...props} className="my-1 text-sm font-semibold" />,
                h5: (props) => <h5 {...props} className="my-1 text-sm font-semibold" />,
                h6: (props) => <h6 {...props} className="my-1 text-sm font-semibold" />,
                p: (props) => <p {...props} className="my-1" />,
                ul: (props) => <ul {...props} className="my-1 list-disc pl-5" />,
                ol: (props) => <ol {...props} className="my-1 list-decimal pl-5" />,
                li: (props) => <li {...props} className="my-0.5" />,
                a: (props) => <a {...props} target="_blank" rel="noopener noreferrer" className="underline" />,
                img: (props) => <img {...props} className="max-w-full rounded" />,
                blockquote: (props) => (
                    <blockquote
                        {...props}
                        className="my-1 border-l-2 pl-2 opacity-80"
                        style={{borderColor: token.colorBorderSecondary}}
                    />
                ),
                hr: (props) => <hr {...props} className="my-2" />,
                table: (props) => (
                    <div className="my-2 overflow-x-auto">
                        <table {...props} className="border-collapse" />
                    </div>
                ),
                th: (props) => (
                    <th
                        {...props}
                        className="border px-2 py-1 text-left font-semibold"
                        style={{borderColor: token.colorBorder}}
                    />
                ),
                td: (props) => (
                    <td
                        {...props}
                        className="border px-2 py-1"
                        style={{borderColor: token.colorBorder}}
                    />
                ),
                pre: (props) => <pre {...props} className="m-0" />,
                code: ({className, children, ...rest}) => {
                    const match = /language-(\w+)/.exec(className ?? "");
                    const codeText = String(children).replace(/\n$/, "");
                    if (match) {
                        return (
                            <CodeBlock code={codeText} dark>
                                <SyntaxHighlighter
                                    PreTag="div"
                                    language={match[1]}
                                    style={oneDark}
                                    customStyle={{margin: 0, borderRadius: 8, fontSize: "0.875rem", padding: "0.75rem"}}
                                >
                                    {codeText}
                                </SyntaxHighlighter>
                            </CodeBlock>
                        );
                    }
                    if (String(children).includes("\n")) {
                        return (
                            <CodeBlock code={codeText}>
                                <code
                                    {...rest}
                                    className="block overflow-x-auto rounded-lg p-3 text-sm"
                                    style={{background: token.colorFillTertiary}}
                                >
                                    {children}
                                </code>
                            </CodeBlock>
                        );
                    }
                    return (
                        <code
                            {...rest}
                            className="rounded px-1 py-0.5 text-sm"
                            style={{background: token.colorFillTertiary}}
                        >
                            {children}
                        </code>
                    );
                },
            }}
        >
            {content}
        </Markdown>
    );
}
