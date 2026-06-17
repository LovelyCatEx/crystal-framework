import {Avatar} from "antd";
import {emptyApiResponseAsync} from "../api/system-request.ts";
import {getResourceFileDownloadUrlById} from "../api/resource/file-resource.api.ts";
import {useSWRState} from "../compositions/use-swr.ts";
import type {ReactNode} from "react";
import {UserOutlined} from "@ant-design/icons";

interface AvatarResourceProps {
    fileEntityId?: string | null;
    url?: string | null;
    defaultIcon?: ReactNode;
    size?: number;
}

export function AvatarResource({ fileEntityId, url, defaultIcon, size }: AvatarResourceProps) {
    const fetchKey = !url && fileEntityId ? `getFileDownloadUrl/${fileEntityId}` : undefined;
    const [resolvedUrl] = useSWRState<string | null>(
        fetchKey,
        () => fileEntityId ? getResourceFileDownloadUrlById(fileEntityId) : emptyApiResponseAsync()
    );

    const finalUrl = url ?? resolvedUrl;

    return <Avatar
        size={size}
        className={finalUrl ? "" : "bg-black/50"}
        src={finalUrl ?? defaultIcon ?? <UserOutlined />}
    />
}
