import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    UserLoginLogManagerController,
    type ManagerReadUserLoginLogDTO,
} from "@/api/user-login-log.api.ts";
import {useEffect, useRef, useState} from "react";
import {useUserLoginLogTableColumns} from "@/components/columns/UserLoginLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";

export function UserLoginLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterUserId, setFilterUserId] = useState<string>();
    const [filterUsername, setFilterUsername] = useState<string>();
    const [filterLoginMethod, setFilterLoginMethod] = useState<number>();
    const [filterSuccess, setFilterSuccess] = useState<string>();
    const [filterRemoteIp, setFilterRemoteIp] = useState<string>();
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
                            defaultValue="-1"
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
                            defaultValue="all"
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