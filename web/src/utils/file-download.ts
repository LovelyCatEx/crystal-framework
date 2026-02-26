import {message} from "antd";

export const downloadJson = (data: any, filename?: string) => {
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename || `data-${Date.now()}.json`;

    document.body.appendChild(link);
    link.click();

    setTimeout(() => {
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    }, 100);
};

export const importJsonFromFile = <T = unknown>(): Promise<T> => {
    return new Promise((resolve, reject) => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.json,application/json';

        input.onchange = (event) => {
            const file = (event.target as HTMLInputElement).files?.[0];
            if (!file) {
                reject(new Error('未选择文件'));
                return;
            }

            const reader = new FileReader();

            reader.onload = (e) => {
                try {
                    const content = e.target?.result as string;
                    const jsonData = JSON.parse(content) as T;

                    void message.success('导入成功');
                    resolve(jsonData);
                } catch (error) {
                    void message.error('文件格式错误');
                    reject(error);
                }
            };

            reader.onerror = () => {
                void message.error('文件读取失败');
                reject(new Error('文件读取失败'));
            };

            reader.readAsText(file);
        };

        input.click();
    });
};