import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {AuditLogManagerController, type ManagerReadAuditLogDTO,} from "@/api/audit/audit-log.api.ts";
import {useEffect, useRef, useState} from "react";
import {useAuditLogTableColumns} from "@/components/columns/AuditLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";

export default function AuditLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterAction, setFilterAction] = useState<number>();
    const [filterUserId, setFilterUserId] = useState<string>();
    const [filterUsername, setFilterUsername] = useState<string>();
    const [filterPath, setFilterPath] = useState<string>();
    const [filterRemoteIp, setFilterRemoteIp] = useState<string>();
    const {t} = useTranslation();
    const columns = useAuditLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filterAction, filterUserId, filterUsername, filterPath, filterRemoteIp]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.auditLogManager.title')}
                subtitle={t('pages.auditLogManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.auditLog')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                editModalFormChildren={<></>}
                query={async (props: ManagerReadAuditLogDTO) => {
                    return (await AuditLogManagerController.query(props)).data!;
                }}
                delete={async () => { return null; }}
                update={async () => { return null; }}
                create={async () => { return null; }}
                tableActions={[
                    {
                        label: <span>{t('pages.auditLogManager.filter.action')}</span>,
                        children: <Select
                            defaultValue="-1"
                            style={{ width: 120 }}
                            options={[
                                { value: '-1', label: t('pages.auditLogManager.filter.all') },
                                { value: '1', label: t('pages.auditLogManager.actionType.create') },
                                { value: '2', label: t('pages.auditLogManager.actionType.read') },
                                { value: '3', label: t('pages.auditLogManager.actionType.update') },
                                { value: '4', label: t('pages.auditLogManager.actionType.delete') },
                            ]}
                            onChange={(value) => setFilterAction(value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                        queryParamsProvider() {
                            return {
                                action: filterAction
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.userIdPlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterUserId((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterUserId(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                userId: filterUserId
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.username')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.usernamePlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterUsername((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterUsername(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                username: filterUsername
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.path')}</span>,
                        children: <Input
                            style={{ width: 160 }}
                            placeholder={t('pages.auditLogManager.filter.pathPlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterPath((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterPath(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                path: filterPath
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.remoteIp')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.remoteIpPlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterRemoteIp((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterRemoteIp(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                remoteIp: filterRemoteIp
                            };
                        }
                    }
                ]}
            >
            </ManagerPageContainer>
        </>
    );
}
