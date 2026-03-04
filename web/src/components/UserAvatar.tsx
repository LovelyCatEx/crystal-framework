import {Avatar} from "antd";
import {emptyApiResponseAsync} from "../api/system-request.ts";
import {managerGetFileDownloadUrl} from "../api/file-resource.api.ts";
import {UserOutlined} from "@ant-design/icons";
import {useSWRState} from "../compositions/swr.ts";

export function UserAvatar({ fileEntityId }: { fileEntityId?: string | null }) {
    const [avatarUrl] = useSWRState<string | null>(
        fileEntityId ? `getFileDownloadUrl/${fileEntityId}` : undefined,
        () => fileEntityId ? managerGetFileDownloadUrl(fileEntityId) : emptyApiResponseAsync()
    )

    return <Avatar
        className={avatarUrl ? "" : "bg-black/50"}
        src={avatarUrl ?? <UserOutlined />}
    />
}