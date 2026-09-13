/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {ReactNode} from "react";
import {ApartmentOutlined, LinkOutlined, TeamOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

export interface TenantBenefitKeyTranslation {
    name: string;
    description: string;
}

export interface TenantBenefitGroupTranslation {
    label: string;
    icon?: ReactNode;
}

const I18N_NAMESPACE = "pages.tenantTireBenefitValueManager";

export function useTenantBenefitKeyToTranslationMap(): Map<string, TenantBenefitKeyTranslation> {
    const {t} = useTranslation();

    const featureKeys: string[] = [
        "invitation.enabled",
        "invitation.max_count",
        "invitation.per_day_count",
        "invitation.per_code_usage_limit",
        "invitation.max_validity_days",
        "member.max_count",
        "department.max_count",
    ];

    const map = new Map<string, TenantBenefitKeyTranslation>();
    for (const key of featureKeys) {
        map.set(key, {
            name: t(`${I18N_NAMESPACE}.keys.${key}.name`),
            description: t(`${I18N_NAMESPACE}.keys.${key}.description`),
        });
    }
    return map;
}

export function useTenantBenefitGroupToTranslationMap(): Map<string, TenantBenefitGroupTranslation> {
    const {t} = useTranslation();

    const map = new Map<string, TenantBenefitGroupTranslation>([
        ["invitation", {label: t(`${I18N_NAMESPACE}.groups.invitation`), icon: <LinkOutlined/>}],
        ["member", {label: t(`${I18N_NAMESPACE}.groups.member`), icon: <TeamOutlined/>}],
        ["department", {label: t(`${I18N_NAMESPACE}.groups.department`), icon: <ApartmentOutlined/>}],
    ]);
    return map;
}

export function getBenefitGroupKey(featureKey: string): string {
    return featureKey.split(".")[0] ?? featureKey;
}
