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

    connect(url: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.ws = new WebSocket(url);
            this.ws.onopen = () => {
                this.initialize().then(() => {
                    this.initialized = true;
                    resolve();
                });
            };
            this.ws.onerror = () => reject(new Error('WebSocket connection failed'));
            this.ws.onmessage = (event) => this.handleMessage(event.data as string);
            this.ws.onclose = () => {
                this.initialized = false;
            };
        });
    }

    disconnect() {
        this.ws?.close();
        this.ws = null;
        this.initialized = false;
    }

    isConnected() {
        return this.initialized && this.ws?.readyState === WebSocket.OPEN;
    }

    setOnDiagnostics(callback: (params: LspDiagnosticsParams) => void) {
        this.onDiagnostics = callback;
    }

    private async initialize() {
        await this.sendRequest('initialize', {
            processId: null,
            capabilities: {
                textDocument: {
                    completion: {
                        completionItem: {
                            snippetSupport: true,
                        }
                    },
                    synchronization: {
                        didSave: true,
                        dynamicRegistration: false,
                    },
                    publishDiagnostics: {
                        relatedInformation: true,
                    }
                },
                workspace: {}
            },
            rootUri: 'file:///workspace',
            workspaceFolders: [{ uri: 'file:///workspace', name: 'workspace' }]
        });

        this.sendNotification('initialized', {});
    }

    openDocument(content: string) {
        this.documentVersion = 1;
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
        if (!this.isConnected()) return [];

        const result = await this.sendRequest('textDocument/completion', {
            textDocument: { uri: this.documentUri },
            position: { line: line - 1, character: character - 1 }, // LSP is 0-indexed
        }) as { items?: LspCompletionItem[] } | LspCompletionItem[] | null;

        if (!result) return [];
        if (Array.isArray(result)) return result;
        return result.items ?? [];
    }

    private sendRequest(method: string, params: unknown): Promise<unknown> {
        return new Promise((resolve) => {
            const id = ++this.requestId;
            this.pendingRequests.set(id, resolve);
            this.send({ jsonrpc: '2.0', id, method, params });
        });
    }

    private sendNotification(method: string, params: unknown) {
        this.send({ jsonrpc: '2.0', method, params });
    }

    private send(message: object) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        }
    }

    private handleMessage(data: string) {
        try {
            const message = JSON.parse(data);

            if (message.id !== undefined && this.pendingRequests.has(message.id)) {
                const callback = this.pendingRequests.get(message.id)!;
                this.pendingRequests.delete(message.id);
                callback(message.result);
            } else if (message.method === 'textDocument/publishDiagnostics') {
                this.onDiagnostics?.(message.params);
            }
        } catch {
            // ignore parse errors
        }
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
