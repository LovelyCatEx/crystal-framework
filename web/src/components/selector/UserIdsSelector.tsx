import {useTranslation} from "react-i18next";
import {EntityIdsSelector} from "./EntityIdsSelector.tsx";
import {useUserTableColumns} from "../columns/UserEntityColumns.tsx";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";
import {UserChip} from "@/components/chip/UserChip.tsx";

interface UserIdsSelectorProps {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function UserIdsSelector({ value, onChange }: UserIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useUserTableColumns();

    return (
        <EntityIdsSelector<User>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.user')}
            columns={columns}
            query={async (params) => (await UserManagerController.query(params)).data!}
            getById={(id) => UserManagerController.getById(id)}
            renderItem={(user) => (
                <UserChip userId={user.id} avatar={user.avatar} name={user.nickname || user.username} />
            )}
        />
    );
}
