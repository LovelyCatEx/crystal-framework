import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {UserLoginLogManagerController} from "@/api/user/user-login-log.api.ts";
import type {ManagerReadUserLoginLogDTO} from "@/types/user/user-login-log.types.ts";
import {useEffect, useRef, useState} from "react";
import {useUserLoginLogTableColumns} from "@/components/columns/UserLoginLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function UserLoginLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { getInitialParam, syncToUrl, initialQueryValues } = useManagerQueryParams();

    const [filterUserId, setFilterUserId] = useState<string | undefined>(getInitialParam('userId'));
    const [filterUsername, setFilterUsername] = useState<string | undefined>(getInitialParam('username'));
    const [filterLoginMethod, setFilterLoginMethod] = useState<number | undefined>(() => {
        const v = getInitialParam('loginMethod');
        return v !== undefined ? Number.parseInt(v, 10) : undefined;
    });
    const [filterSuccess, setFilterSuccess] = useState<string | undefined>(getInitialParam('success'));
    const [filterRemoteIp, setFilterRemoteIp] = useState<string | undefined>(getInitialParam('remoteIp'));
    const {t} = useTranslation();
    const columns = useUserLoginLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filterUserId, filterUsername, filterLoginMethod, filterSuccess, filterRemoteIp]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.userLoginLogManager.title')}
                subtitle={t('pages.userLoginLogManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.userLoginLog')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                editModalFormChildren={<></>}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                query={async (props: ManagerReadUserLoginLogDTO) => {
                    return (await UserLoginLogManagerController.query(props)).data!;
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
                        label: <span>{t('pages.userLoginLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.userIdPlaceholder')}
                            defaultValue={filterUserId}
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
                        label: <span>{t('pages.userLoginLogManager.filter.username')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.usernamePlaceholder')}
                            defaultValue={filterUsername}
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
                        label: <span>{t('pages.userLoginLogManager.filter.loginMethod')}</span>,
                        children: <Select
                            defaultValue={filterLoginMethod !== undefined ? String(filterLoginMethod) : "-1"}
                            style={{width: 120}}
                            options={[
                                {value: '-1', label: t('pages.userLoginLogManager.filter.all')},
                                {value: '0', label: t('pages.userLoginLogManager.loginMethod.password')},
                                {value: '1', label: t('pages.userLoginLogManager.loginMethod.oauth2')},
                            ]}
                            onChange={(value) => setFilterLoginMethod(value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                        queryParamsProvider() {
                            return {
                                loginMethod: filterLoginMethod
                            };
                        }
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.status')}</span>,
                        children: <Select
                            defaultValue={filterSuccess ?? "all"}
                            style={{width: 100}}
                            options={[
                                {value: 'all', label: t('pages.userLoginLogManager.filter.all')},
                                {value: 'true', label: t('pages.userLoginLogManager.filter.success')},
                                {value: 'false', label: t('pages.userLoginLogManager.filter.failed')},
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
                        label: <span>{t('pages.userLoginLogManager.filter.remoteIp')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.remoteIpPlaceholder')}
                            defaultValue={filterRemoteIp}
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
            />
        </>
    );
}