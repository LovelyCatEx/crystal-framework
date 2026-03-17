import {UserOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useUserTableColumns} from "../columns/UserEntityColumns.tsx";
import {UserManagerController} from "@/api/user.api.ts";
import type {User} from "@/types/user.types.ts";

interface UserIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    isRowDisabled?: (row: User, value?: string | null) => boolean;
}

export function UserIdSelector({ value, onChange, isRowDisabled }: UserIdSelectorProps) {
    const { t } = useTranslation();
    const columns = useUserTableColumns();
    
    return (
        <EntityIdSelector<User>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.user')}
            columns={columns}
            controller={UserManagerController}
            displayRender={(user) => `${user.username} (${user.email})`}
            placeholder={t('components.selector.entityIdSelector.placeholder')}
            isRowDisabled={(row) => {
                if (isRowDisabled) {
                    return isRowDisabled(row, value);
                } else {
                    return false;
                }
            }}
            icon={<UserOutlined />}
        />
    );
}
