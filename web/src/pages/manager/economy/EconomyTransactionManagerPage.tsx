/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    EconomyTransactionManagerController,
    type ManagerReadEconomyTransactionDTO
} from "@/api/economy/transaction.api.ts";
import {useRef} from "react";
import {useEconomyTransactionTableColumns} from "@/components/columns/EconomyTransactionEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

export default function EconomyTransactionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useEconomyTransactionTableColumns();

    return (
        <>
            <ActionBarComponent
                title={t('pages.transactionManager.title')}
                subtitle={t('pages.transactionManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.transaction')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                editModalFormChildren={<></>}
                query={async (props) => {
                    const dto: ManagerReadEconomyTransactionDTO = { ...props, scope: ResourceScope.SYSTEM };
                    return (await EconomyTransactionManagerController.query(dto)).data!
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
            />
        </>
    );
}
