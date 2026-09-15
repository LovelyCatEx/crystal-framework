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
import {useEconomyTransactionTableColumns} from "@/components/columns/EconomyTransactionEntityColumns.tsx";
import {EconomyTransactionManagerController, type ManagerReadEconomyTransactionDTO} from "@/api/economy/transaction.api.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

export default function TenantEconomyTransactionManagerPage() {
    const {t} = useTranslation();
    const columns = useEconomyTransactionTableColumns();
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
                title={t('pages.tenantTransactionManager.title')}
                subtitle={t('pages.tenantTransactionManager.subtitle')}
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={setSelectedTenantId}
            />
            {selectedTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.transaction')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode={true}
                    showRowActions={false}
                    columns={columns}
                    editModalFormChildren={<></>}
                    query={async (props) => {
                        const dto: ManagerReadEconomyTransactionDTO = { ...props, scope: ResourceScope.TENANT, scopeId: selectedTenantId };
                        return (await EconomyTransactionManagerController.query(dto)).data!
                    }}
                    delete={async () => null}
                    update={async () => null}
                    create={async () => null}
                />
            )}
        </>
    );
}
