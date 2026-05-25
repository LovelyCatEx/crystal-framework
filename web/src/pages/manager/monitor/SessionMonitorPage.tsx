import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {EntityTable} from "@/components/table/EntityTable.tsx";
import {useSessionMonitorTableColumns} from "@/components/columns/SessionMonitorEntityColumns.tsx";
import {getOnlineSessions} from "@/api/monitor/session-monitor.api.ts";
import {useTranslation} from "react-i18next";
import type {SessionDescription} from "@/types/system/session.types.ts";
import type {BaseManagerReadDTO} from "@/types/api.types.ts";
import {StandardCard} from "@/components/card/StandardCard.tsx";

export function SessionMonitorPage() {
    const {t} = useTranslation();
    const columns = useSessionMonitorTableColumns();

    const handleQuery = async (props: BaseManagerReadDTO) => {
        return await getOnlineSessions({
            page: props.page ?? 1,
            pageSize: props.pageSize ?? 20,
            sessionId: props.searchKeyword
        });
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.sessionMonitor.title')}
                subtitle={t('pages.sessionMonitor.subtitle')}
            />

            <StandardCard>
                <EntityTable<SessionDescription>
                    entityName={t('pages.sessionMonitor.entityName')}
                    columns={columns}
                    query={handleQuery}
                />
            </StandardCard>
        </>
    );
}