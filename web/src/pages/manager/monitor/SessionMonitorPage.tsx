import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {EntityTable} from "@/components/table/EntityTable.tsx";
import {useSessionMonitorTableColumns} from "@/components/columns/SessionMonitorEntityColumns.tsx";
import {getOnlineSessions} from "@/api/monitor/session-monitor.api.ts";
import {useTranslation} from "react-i18next";
import type {SessionDescription} from "@/types/system/session.types.ts";
import type {BaseManagerReadDTO} from "@/types/api.types.ts";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import type {ConditionNode, GroupNode} from "@/components/table/filter/filter-builder.types.ts";

/**
 * Extract the first 'contains' value for a given field from a QueryNode tree.
 * Used to pull the sessionId search value from the merged query node.
 */
function extractContainsValue(node: GroupNode | undefined, field: string): string | undefined {
    if (!node) return undefined;
    for (const child of node.children) {
        if (child.type === 'condition') {
            const cond = child as ConditionNode;
            if (cond.field === field && cond.operator === 'contains' && cond.value) {
                return String(cond.value);
            }
        } else if (child.type === 'group') {
            const found = extractContainsValue(child as GroupNode, field);
            if (found) return found;
        }
    }
    return undefined;
}

export default function SessionMonitorPage() {
    const {t} = useTranslation();
    const columns = useSessionMonitorTableColumns();

    const handleQuery = async (props: BaseManagerReadDTO) => {
        const sessionId = extractContainsValue(props.query as GroupNode | undefined, 'session_id');
        return await getOnlineSessions({
            page: props.page ?? 1,
            pageSize: props.pageSize ?? 20,
            sessionId,
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
                    hideRecordTimeColumn={true}
                    searchKeywords={['session_id']}
                />
            </StandardCard>
        </>
    );
}