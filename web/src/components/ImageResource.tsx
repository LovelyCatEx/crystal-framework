import { Image } from "antd";
import { emptyApiResponseAsync } from "../api/system-request.ts";
import { getResourceFileDownloadUrlById } from "../api/resource/file-resource.api.ts";
import { useSWRState } from "../compositions/use-swr.ts";
import type { CSSProperties } from "react";

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
    const [imageUrl] = useSWRState<string | null>(
        fileEntityId ? `imageResource/${fileEntityId}` : undefined,
        () => fileEntityId ? getResourceFileDownloadUrlById(fileEntityId) : emptyApiResponseAsync()
    );

    if (!imageUrl) return null;

    return (
        <Image
            className={className}
            src={imageUrl}
            width={width}
            height={height}
            fallback={fallback}
            style={style}
            preview={preview}
        />
    );
}
