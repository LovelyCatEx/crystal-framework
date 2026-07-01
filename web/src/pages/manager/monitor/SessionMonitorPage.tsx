import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {EntityTable, type EntityTableRef} from "@/components/table/EntityTable.tsx";
import {useSessionMonitorTableColumns} from "@/components/columns/SessionMonitorEntityColumns.tsx";
import {getOnlineSessions} from "@/api/monitor/session-monitor.api.ts";
import {useTranslation} from "react-i18next";
import {useEffect, useRef, useState} from "react";
import {Select} from "antd";
import type {SessionDescription} from "@/types/system/session.types.ts";
import {SessionType} from "@/types/system/session.types.ts";
import {getSessionType} from "@/i18n/enum-helpers.ts";
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
    const tableRef = useRef<EntityTableRef | null>(null);
    const [selectedType, setSelectedType] = useState<number | undefined>(undefined);
    const selectedTypeRef = useRef<number | undefined>(undefined);
    selectedTypeRef.current = selectedType;

    // Refresh when selectedType changes; the query fetcher reads selectedTypeRef.current.
    useEffect(() => {
        tableRef.current?.refreshData({resetPage: true});
    }, [selectedType]);

    const handleQuery = async (props: BaseManagerReadDTO) => {
        const sessionId = extractContainsValue(props.query as GroupNode | undefined, 'session_id');
        return await getOnlineSessions({
            page: props.page ?? 1,
            pageSize: props.pageSize ?? 20,
            sessionId,
            type: selectedTypeRef.current,
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
                    ref={tableRef}
                    entityName={t('pages.sessionMonitor.entityName')}
                    columns={columns}
                    query={handleQuery}
                    hideRecordTimeColumn={true}
                    searchKeywords={['session_id']}
                    tableActions={[
                        {
                            label: <span>{t('pages.sessionMonitor.filter.type')}</span>,
                            children: <Select
                                className="w-40"
                                placeholder={t('pages.sessionMonitor.filter.typePlaceholder')}
                                allowClear
                                value={selectedType}
                                onChange={(value) => setSelectedType(value ?? undefined)}
                                options={[
                                    {value: SessionType.USER, label: getSessionType(SessionType.USER)},
                                    {value: SessionType.PROMETHEUS, label: getSessionType(SessionType.PROMETHEUS)},
                                ]}
                            />,
                        },
                    ]}
                />
            </StandardCard>
        </>
    );
}