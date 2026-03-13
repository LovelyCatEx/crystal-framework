import {Avatar} from "antd";
import {emptyApiResponseAsync} from "../api/system-request.ts";
import {managerGetFileDownloadUrl} from "../api/file-resource.api.ts";
import {useSWRState} from "../compositions/swr.ts";
import type {ReactNode} from "react";
import {UserOutlined} from "@ant-design/icons";

export function AvatarResource({ fileEntityId, defaultIcon }: { fileEntityId?: string | null, defaultIcon?: ReactNode }) {
    const [avatarUrl] = useSWRState<string | null>(
        fileEntityId ? `getFileDownloadUrl/${fileEntityId}` : undefined,
        () => fileEntityId ? managerGetFileDownloadUrl(fileEntityId) : emptyApiResponseAsync()
    )

    return <Avatar
        className={avatarUrl ? "" : "bg-black/50"}
        src={avatarUrl ?? defaultIcon ?? <UserOutlined />}
    />
}