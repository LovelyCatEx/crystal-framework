import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {MailSendLogManagerController, type ManagerReadMailSendLogDTO,} from "@/api/mail-send-log.api.ts";
import {useEffect, useRef, useState} from "react";
import {useMailSendLogTableColumns} from "@/components/columns/MailSendLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";

export function MailSendLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterKeyword, setFilterKeyword] = useState<string>();
    const [filterToEmail, setFilterToEmail] = useState<string>();
    const [filterSuccess, setFilterSuccess] = useState<string>();
    const [filterUserId, setFilterUserId] = useState<string>();
    const {t} = useTranslation();
    const columns = useMailSendLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filterKeyword, filterToEmail, filterSuccess, filterUserId]);

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
                query={async (props: ManagerReadMailSendLogDTO) => {
                    return (await MailSendLogManagerController.query(props)).data!;
                }}
                delete={async () => {
                    return null;
                }}
                update={async () => {
                    return null;
                }}
                create={async () => {
                    return null;
                }}
                tableActions={[
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.keyword')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.mailSendLogManager.filter.keywordPlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterKeyword((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterKeyword(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                keyword: filterKeyword
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.toEmail')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.mailSendLogManager.filter.toEmailPlaceholder')}
                            allowClear
                            onPressEnter={(e) => setFilterToEmail((e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilterToEmail(undefined);
                            }}
                        />,
                        queryParamsProvider() {
                            return {
                                toEmail: filterToEmail
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.status')}</span>,
                        children: <Select
                            defaultValue="all"
                            style={{width: 100}}
                            options={[
                                {value: 'all', label: t('pages.mailSendLogManager.filter.all')},
                                {value: 'true', label: t('pages.mailSendLogManager.filter.success')},
                                {value: 'false', label: t('pages.mailSendLogManager.filter.failed')},
                            ]}
                            onChange={(value) => setFilterSuccess(value === 'all' ? undefined : value)}
                        />,
                        queryParamsProvider() {
                            return {
                                success: filterSuccess === 'true' ? true : filterSuccess === 'false' ? false : undefined
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.mailSendLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.mailSendLogManager.filter.userIdPlaceholder')}
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
                    }
                ]}
            />
        </>
    );
}
