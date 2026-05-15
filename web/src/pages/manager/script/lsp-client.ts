/**
 * Lightweight LSP client over WebSocket.
 * Implements just enough of the LSP protocol to provide completion and diagnostics.
 */

type LspCallback = (result: unknown) => void;

export class LspClient {
    private ws: WebSocket | null = null;
    private requestId = 0;
    private pendingRequests = new Map<number, LspCallback>();
    private onDiagnostics: ((params: LspDiagnosticsParams) => void) | null = null;
    private documentVersion = 0;
    private documentUri = 'file:///workspace/Script.kt';
    private initialized = false;
    
    // Debounce state for completions
    private completionDebounceTimer: ReturnType<typeof setTimeout> | null = null;
    private pendingCompletionRequestId: number | null = null;

    connect(url: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.log('info', `正在连接 LSP 服务器: ${url}`);
            this.ws = new WebSocket(url);
            this.ws.onopen = () => {
                this.log('success', 'WebSocket 连接成功 ✅');
                this.initialize().then(() => {
                    this.initialized = true;
                    this.log('success', 'LSP 初始化完成，可以使用代码提示了 ✅');
                    resolve();
                }).catch((err) => {
                    this.log('error', `LSP 初始化失败: ${err.message}`);
                    reject(err);
                });
            };
            this.ws.onerror = (e) => {
                this.log('error', `WebSocket 连接错误 ❌\n- 错误事件: ${e.type}\n- 可能原因: 后端服务未启动或端口配置错误`);
                reject(new Error('WebSocket connection failed'));
            };
            this.ws.onclose = (e) => {
                const closeReason = this.getCloseReason(e.code);
                this.log('warning', `WebSocket 连接已关闭 ⚠️\n- 关闭码: ${e.code}\n- 原因: ${closeReason}`);
                this.initialized = false;
            };
            this.ws.onmessage = (event) => {
                this.handleMessage(event.data as string);
            };
        });
    }

    private getCloseReason(code: number): string {
        switch (code) {
            case 1000: return '正常关闭';
            case 1006: return '异常关闭（可能是服务端断开或网络问题）';
            case 1011: return '服务端内部错误';
            default: return code === 1005 ? '无状态码' : `未知 (${code})`;
        }
    }

    disconnect() {
        this.log('info', '手动断开 LSP 连接');
        this.ws?.close();
        this.ws = null;
        this.initialized = false;
        if (this.completionDebounceTimer) {
            clearTimeout(this.completionDebounceTimer);
        }
    }

    isConnected() {
        return this.initialized && this.ws?.readyState === WebSocket.OPEN;
    }

    setOnDiagnostics(callback: (params: LspDiagnosticsParams) => void) {
        this.onDiagnostics = callback;
    }

    private async initialize() {
        this.log('info', '正在初始化 LSP 会话...');
        await this.sendRequest('initialize', {
            processId: null,
            capabilities: {
                textDocument: {
                    completion: {
                        completionItem: {
                            snippetSupport: true,
                            commitCharactersSupport: true,
                            documentationFormat: ['markdown', 'plaintext'],
                        },
                        contextSupport: true,
                    },
                    synchronization: {
                        didSave: true,
                        dynamicRegistration: false,
                    },
                    publishDiagnostics: {
                        relatedInformation: true,
                    }
                },
                workspace: {
                    workspaceFolders: true,
                }
            },
            rootUri: 'file:///workspace',
            workspaceFolders: [{ uri: 'file:///workspace', name: 'crystal-script-sdk' }]
        });

        this.sendNotification('initialized', {});
    }

    openDocument(content: string) {
        this.documentVersion = 1;
        this.log('info', `📄 打开文档，版本: ${this.documentVersion}`);
        this.sendNotification('textDocument/didOpen', {
            textDocument: {
                uri: this.documentUri,
                languageId: 'kotlin',
                version: this.documentVersion,
                text: content,
            }
        });
    }

    updateDocument(content: string) {
        this.documentVersion++;
        this.sendNotification('textDocument/didChange', {
            textDocument: {
                uri: this.documentUri,
                version: this.documentVersion,
            },
            contentChanges: [{ text: content }]
        });
    }

    async getCompletions(line: number, character: number): Promise<LspCompletionItem[]> {
        if (!this.isConnected()) {
            this.log('warning', '⚠️ 未连接 LSP，无法获取补全建议');
            return [];
        }

        this.log('info', `🔍 请求补全 - 位置: 第 ${line} 行, 第 ${character} 列`);
        const result = await this.sendRequest('textDocument/completion', {
            textDocument: { uri: this.documentUri },
            position: { line: line - 1, character: character - 1 },
        }, 3000) as { items?: LspCompletionItem[] } | LspCompletionItem[] | null;

        if (!result) return [];
        if (Array.isArray(result)) return result;
        return result.items ?? [];
    }

    async getCompletionsDebounced(line: number, character: number, delay: number = 150): Promise<LspCompletionItem[]> {
        return new Promise((resolve) => {
            if (this.completionDebounceTimer) {
                clearTimeout(this.completionDebounceTimer);
            }

            if (this.pendingCompletionRequestId !== null) {
                this.pendingRequests.delete(this.pendingCompletionRequestId);
                this.pendingCompletionRequestId = null;
                this.log('debug', '⏭️ 取消旧的补全请求');
            }

            this.completionDebounceTimer = setTimeout(async () => {
                this.completionDebounceTimer = null;
                
                if (!this.isConnected()) {
                    resolve([]);
                    return;
                }

                const id = ++this.requestId;
                this.pendingCompletionRequestId = id;
                this.log('info', `🔍 请求补全 (防抖后) - 位置: ${line}:${character}`);

                try {
                    const result = await this.sendRequestWithId(id, 'textDocument/completion', {
                        textDocument: { uri: this.documentUri },
                        position: { line: line - 1, character: character - 1 },
                    }, 2000);

                    if (this.pendingCompletionRequestId === id) {
                        this.pendingCompletionRequestId = null;
                        const items = (!result) ? [] : (Array.isArray(result) ? result : (result as { items?: LspCompletionItem[] }).items ?? []);
                        this.log('success', `✅ 补全响应 - 获取到 ${items.length} 个建议`);
                        resolve(items);
                    } else {
                        this.log('debug', '🔄 忽略过期的补全响应');
                        resolve([]);
                    }
                } catch (err) {
                    this.pendingCompletionRequestId = null;
                    this.log('error', `❌ 补全请求失败: ${err}`);
                    resolve([]);
                }
            }, delay);
        });
    }

    private async sendRequest(method: string, params: unknown, timeout: number = 5000): Promise<unknown> {
        return new Promise((resolve) => {
            const id = ++this.requestId;
            const startTime = Date.now();

            const timeoutTimer = setTimeout(() => {
                this.pendingRequests.delete(id);
                this.log('warning', `⏰ 请求超时: ${method} (${timeout}ms)`);
                resolve(null);
            }, timeout);

            const callback: LspCallback = (result) => {
                clearTimeout(timeoutTimer);
                const duration = Date.now() - startTime;
                this.log('debug', `📤 响应耗时: ${method} - ${duration}ms`);
                resolve(result);
            };

            this.pendingRequests.set(id, callback);
            this.send({ jsonrpc: '2.0', id, method, params });
        });
    }

    private async sendRequestWithId(id: number, method: string, params: unknown, timeout: number): Promise<unknown> {
        return new Promise((resolve) => {
            const startTime = Date.now();

            const timeoutTimer = setTimeout(() => {
                this.pendingRequests.delete(id);
                this.log('warning', `⏰ 请求超时: ${method} (${timeout}ms)`);
                resolve(null);
            }, timeout);

            const callback: LspCallback = (result) => {
                clearTimeout(timeoutTimer);
                const duration = Date.now() - startTime;
                this.log('debug', `📤 响应耗时: ${method} - ${duration}ms`);
                resolve(result);
            };

            this.pendingRequests.set(id, callback);
            this.send({ jsonrpc: '2.0', id, method, params });
        });
    }

    private sendNotification(method: string, params: unknown) {
        this.send({ jsonrpc: '2.0', method, params });
    }

    private send(message: object) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            const text = JSON.stringify(message);
            this.log('debug', `→ 发送消息: ${text.substring(0, 150)}${text.length > 150 ? '...' : ''}`);
            this.ws.send(text);
        } else {
            this.log('warning', `⚠️ 无法发送消息，WebSocket 状态: ${this.getReadyStateText(this.ws?.readyState)}`);
        }
    }

    private getReadyStateText(state: number | undefined): string {
        switch (state) {
            case 0: return 'CONNECTING (连接中)';
            case 1: return 'OPEN (已打开)';
            case 2: return 'CLOSING (关闭中)';
            case 3: return 'CLOSED (已关闭)';
            default: return 'UNKNOWN (未知)';
        }
    }

    private handleMessage(data: string) {
        try {
            const message = JSON.parse(data);

            if (message.id !== undefined) {
                this.log('debug', `← 收到响应: id=${message.id}`);
                if (this.pendingRequests.has(message.id)) {
                    const callback = this.pendingRequests.get(message.id)!;
                    this.pendingRequests.delete(message.id);
                    callback(message.result);
                }
            } else if (message.method === 'textDocument/publishDiagnostics') {
                const diagnostics = message.params?.diagnostics || [];
                const errors = diagnostics.filter((d: LspDiagnostic) => d.severity === 1).length;
                const warnings = diagnostics.filter((d: LspDiagnostic) => d.severity === 2).length;
                this.log('info', `📊 收到诊断信息 - 错误: ${errors}, 警告: ${warnings}`);
                this.onDiagnostics?.(message.params);
            } else if (message.method) {
                this.log('debug', `← 收到通知: ${message.method}`);
            }
        } catch (err) {
            this.log('error', `❌ 解析消息失败: ${err}`);
        }
    }

    private log(level: 'info' | 'success' | 'warning' | 'error' | 'debug', message: string) {
        const colors: Record<string, string> = {
            info: '%c[LSP]',
            success: '%c[LSP ✅]',
            warning: '%c[LSP ⚠️]',
            error: '%c[LSP ❌]',
            debug: '%c[LSP 📝]'
        };

        const colorStyles: Record<string, string> = {
            info: 'color: #2196F3; font-weight: bold;',
            success: 'color: #4CAF50; font-weight: bold;',
            warning: 'color: #FF9800; font-weight: bold;',
            error: 'color: #F44336; font-weight: bold;',
            debug: 'color: #9E9E9E; font-weight: bold;'
        };

        console.log(colors[level], colorStyles[level], message);
    }
}

export interface LspDiagnosticsParams {
    uri: string;
    diagnostics: LspDiagnostic[];
}

export interface LspDiagnostic {
    range: {
        start: { line: number; character: number };
        end: { line: number; character: number };
    };
    severity?: number; // 1=Error, 2=Warning, 3=Info, 4=Hint
    message: string;
}

export interface LspCompletionItem {
    label: string;
    kind?: number;
    detail?: string;
    insertText?: string;
    insertTextFormat?: number; // 1=PlainText, 2=Snippet
}