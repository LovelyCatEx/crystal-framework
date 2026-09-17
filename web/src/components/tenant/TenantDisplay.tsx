/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Spin} from "antd";
import {ShopOutlined} from "@ant-design/icons";
import {AvatarResource} from "@/components/resource/AvatarResource.tsx";
import {useTenantById} from "@/compositions/use-tenant-by-id.ts";

export function TenantDisplay({tenantId, variant = 'text'}: {
    tenantId: string | null;
    variant?: 'text' | 'icon-text';
}) {
    const {tenant, isLoading} = useTenantById(tenantId);

    if (!tenantId) return <span>—</span>;
    if (isLoading) return <Spin size="small" />;

    const label = tenant?.name || tenantId;

    if (variant === 'icon-text') {
        return (
            <span className="inline-flex items-center gap-1">
                {tenant?.icon
                    ? <AvatarResource fileEntityId={tenant.icon} defaultIcon={<ShopOutlined />} size={20} />
                    : <ShopOutlined style={{fontSize: 18}} />}
                <span>{label}</span>
            </span>
        );
    }

    return <span>{label}</span>;
}
