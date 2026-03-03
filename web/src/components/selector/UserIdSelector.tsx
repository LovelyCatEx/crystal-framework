import {UserOutlined} from "@ant-design/icons";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {USER_MANAGER_TABLE_COLUMNS} from "../columns/UserEntityColumns.tsx";
import {UserManagerController} from "../../api/user.api.ts";
import type {User} from "../../types/user.types.ts";

interface UserIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
}

export function UserIdSelector({ value, onChange }: UserIdSelectorProps) {
    return (
        <EntityIdSelector<User>
            value={value}
            onChange={onChange}
            entityName="用户"
            columns={USER_MANAGER_TABLE_COLUMNS}
            controller={UserManagerController}
            displayRender={(user) => `${user.username} (${user.email})`}
            placeholder="选择用户"
            icon={<UserOutlined />}
        />
    );
}
