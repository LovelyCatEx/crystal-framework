/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useTranslation} from "react-i18next";
import {Tag} from "antd";
import {EntityIdsSelector} from "../EntityIdsSelector.tsx";
import {useTenantRoleTableColumns} from "@/components/columns/TenantRoleEntityColumns.tsx";
import {TenantRoleManagerController} from "@/api/tenant/rbac/tenant-role.api.ts";
import type {TenantRole} from "@/types/tenant/rbac/tenant-role.types.ts";

interface TenantRoleIdsSelectorProps {
    tenantId: string;
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function TenantRoleIdsSelector({ tenantId, value, onChange }: TenantRoleIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useTenantRoleTableColumns();

    return (
        <EntityIdsSelector<TenantRole>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.tenantRole')}
            columns={columns}
            query={async (params) => (await TenantRoleManagerController.query({ ...params, tenantId })).data!}
            getById={(id) => TenantRoleManagerController.getById(id, { tenantId })}
            renderItem={(role) => (
                <Tag key={role.id}>{role.name}</Tag>
            )}
        />
    );
}
