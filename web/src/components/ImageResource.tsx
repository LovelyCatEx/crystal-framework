import {Image, Spin} from "antd";
import {FileImageOutlined} from "@ant-design/icons";
import {emptyApiResponseAsync} from "../api/system-request.ts";
import {getResourceFileDownloadUrlById} from "../api/resource/file-resource.api.ts";
import {useSWRState} from "../compositions/use-swr.ts";
import type {CSSProperties} from "react";

export function ImageResource({
    fileEntityId,
    className,
    width = 60,
    height,
    fallback,
    style,
    preview
}: {
    fileEntityId?: string | null;
    className?: string;
    width?: number | string;
    height?: number | string;
    fallback?: string;
    style?: CSSProperties;
    preview?: Parameters<typeof Image>[number]['preview'];
}) {
    const [imageUrl, , isLoading] = useSWRState<string | null>(
        fileEntityId ? `imageResource/${fileEntityId}` : undefined,
        () => fileEntityId ? getResourceFileDownloadUrlById(fileEntityId) : emptyApiResponseAsync()
    );

    if (isLoading) {
        return (
            <span
                className={className}
                style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: typeof width === 'number' ? `${width}px` : width,
                    height: typeof height === 'number' ? `${height}px` : (height ?? (typeof width === 'number' ? `${width}px` : width)),
                    ...style,
                }}
            >
                <Spin size="small"/>
            </span>
        );
    }

    if (!imageUrl) {
        return (
            <span
                className={className}
                style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: typeof width === 'number' ? `${width}px` : width,
                    height: typeof height === 'number' ? `${height}px` : (height ?? (typeof width === 'number' ? `${width}px` : width)),
                    color: '#999',
                    fontSize: 20,
                    ...style,
                }}
            >
                <FileImageOutlined/>
            </span>
        );
    }

    return (
        <Image
            className={className}
            src={imageUrl}
            width={width}
            height={height}
            fallback={fallback}
            style={{display: 'block', ...style}}
            preview={preview}
        />
    );
}
