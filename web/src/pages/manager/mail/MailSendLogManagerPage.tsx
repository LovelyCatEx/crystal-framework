import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {MailSendLogManagerController} from "@/api/mail/mail-send-log.api.ts";
import type {ManagerReadMailSendLogDTO} from "@/types/mail/mail-send-log.types.ts";
import {useEffect, useRef} from "react";
import {useMailSendLogTableColumns} from "@/components/columns/MailSendLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function MailSendLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter } = useManagerQueryParams({
        schema: {
            keyword: 'string',
            toEmail: 'string',
            success: 'string',
            userId: 'string',
        }
    });
    const {t} = useTranslation();
    const columns = useMailSendLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.keyword, filters.toEmail, filters.success, filters.userId]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.mailSendLogManager.title')}
                subtitle={t('pages.mailSendLogManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.mailSendLog')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                editModalFormChildren={<></>}
                extraQueryParams={{
                    ...filters,
                    success: filters.success === 'true' ? true : filters.success === 'false' ? false : undefined,
                }}
                query={async (props: ManagerReadMailSendLogDTO) => {
                    return (await MailSendLogManagerController.query(props)).data!;
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
                tableActions={[
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.keyword')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.mailSendLogManager.filter.keywordPlaceholder')}
                            defaultValue={filters.keyword}
                            allowClear
                            onPressEnter={(e) => setFilter('keyword', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('keyword', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.toEmail')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.mailSendLogManager.filter.toEmailPlaceholder')}
                            defaultValue={filters.toEmail}
                            allowClear
                            onPressEnter={(e) => setFilter('toEmail', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('toEmail', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.status')}</span>,
                        children: <Select
                            defaultValue={filters.success ?? 'all'}
                            style={{width: 100}}
                            options={[
                                {value: 'all', label: t('pages.mailSendLogManager.filter.all')},
                                {value: 'true', label: t('pages.mailSendLogManager.filter.success')},
                                {value: 'false', label: t('pages.mailSendLogManager.filter.failed')},
                            ]}
                            onChange={(value) => setFilter('success', value === 'all' ? undefined : value)}
                        />,
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.mailSendLogManager.filter.userIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    }
                ]}
            />
        </>
    );
}
