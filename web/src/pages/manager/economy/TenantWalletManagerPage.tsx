/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {useWalletTableColumns} from "@/components/columns/WalletEntityColumns.tsx";
import {WalletManagerController, type ManagerReadWalletDTO} from "@/api/economy/wallet.api.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

export default function TenantWalletManagerPage() {
    const {t} = useTranslation();
    const columns = useWalletTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    useEffect(() => {
        if (selectedTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [selectedTenantId]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantWalletManager.title')}
                subtitle={t('pages.tenantWalletManager.subtitle')}
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={setSelectedTenantId}
            />
            {selectedTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.wallet')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode={true}
                    showRowActions={false}
                    columns={columns}
                    editModalFormChildren={<></>}
                    query={async (props) => {
                        const dto: ManagerReadWalletDTO = { ...props, scope: ResourceScope.TENANT, scopeId: selectedTenantId };
                        return (await WalletManagerController.query(dto)).data!
                    }}
                    delete={async () => null}
                    update={async () => null}
                    create={async () => null}
                />
            )}
        </>
    );
}
