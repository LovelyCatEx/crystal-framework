import {useTranslation} from "react-i18next";
import {Tag} from "antd";
import {EntityIdsSelector} from "../EntityIdsSelector.tsx";
import {useUserRoleTableColumns} from "@/components/columns/UserRoleEntityColumns.tsx";
import {UserRoleManagerController} from "@/api/user/rbac/user-role.api.ts";
import type {UserRole} from "@/types/user/rbac/user-role.types.ts";

interface UserRoleIdsSelectorProps {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function UserRoleIdsSelector({ value, onChange }: UserRoleIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useUserRoleTableColumns();

    return (
        <EntityIdsSelector<UserRole>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.userRole')}
            columns={columns}
            query={async (params) => (await UserRoleManagerController.query(params)).data!}
            getById={(id) => UserRoleManagerController.getById(id)}
            renderItem={(role) => (
                <Tag key={role.id}>{role.name}</Tag>
            )}
        />
    );
}
