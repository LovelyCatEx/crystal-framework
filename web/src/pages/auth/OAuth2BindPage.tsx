import {useEffect, useRef, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {Button, message, Result, Spin} from "antd";
import {bindOAuthByAccountId, loginByOAuth2Code} from "@/api/auth/auth.api.ts";
import {OAuthBindingScope} from "@/types/user/oauth-account.types.ts";
import {menuPathProfile} from "@/router/paths.ts";
import {AuthCardLayout} from "./AuthorizationPage.tsx";

const TENANT_PERSONAL_PROFILE_PATH = '/manager/tenant/personal-profile';

interface BindLocationState {
    code: string;
    state: string;
    registrationId: string;
    scope: OAuthBindingScope;
}

export function OAuth2BindPage() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();

    const locationState = location.state as BindLocationState | null;

    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
    const [errorMessage, setErrorMessage] = useState<string>('');
    const executedRef = useRef(false);

    useEffect(() => {
        if (executedRef.current) return;
        executedRef.current = true;

        if (!locationState?.code || !locationState?.state || locationState?.scope == null) {
            setStatus('error');
            setErrorMessage(t('pages.auth.oauth2Bind.invalidParams'));
            return;
        }

        // Step 1: Exchange code via Spring Security (handles PKCE automatically)
        loginByOAuth2Code(locationState.code, locationState.state)
            .then((res) => {
                if (!res.data) {
                    setStatus('error');
                    setErrorMessage(t('pages.auth.oauth2Bind.failed'));
                    return;
                }

                // Both response types now include oauthAccountId
                const data = res.data as { oauthAccountId?: string };
                const oauthAccountId = data.oauthAccountId;
                if (!oauthAccountId) {
                    setStatus('error');
                    setErrorMessage(t('pages.auth.oauth2Bind.failed'));
                    return;
                }

                // Step 2: Bind the resolved identity to the current user at the specified scope
                return bindOAuthByAccountId({
                    oauthAccountId,
                    scope: locationState.scope,
                });
            })
            .then((bindRes) => {
                if (!bindRes) return; // Early exit from step 1 failure
                if (bindRes.data != null) {
                    setStatus('success');
                    void message.success(t('pages.auth.oauth2Bind.success'));
                } else {
                    setStatus('error');
                    setErrorMessage(bindRes.message || t('pages.auth.oauth2Bind.failed'));
                }
            })
            .catch(() => {
                setStatus('error');
                setErrorMessage(t('pages.auth.oauth2Bind.failed'));
            });
    }, []);

    const returnPath = locationState?.scope === OAuthBindingScope.TENANT
        ? TENANT_PERSONAL_PROFILE_PATH
        : menuPathProfile;

    const handleReturn = () => {
        navigate(returnPath, {replace: true});
    };

    if (status === 'loading') {
        return (
            <AuthCardLayout
                title={t('pages.auth.oauth2Bind.title')}
                subtitle={t('pages.auth.oauth2Bind.subtitle')}
            >
                <div className="flex flex-col items-center justify-center py-12">
                    <Spin size="large"/>
                    <p className="mt-4 text-gray-500">{t('pages.auth.oauth2Bind.processing')}</p>
                </div>
            </AuthCardLayout>
        );
    }

    return (
        <AuthCardLayout
            title={t('pages.auth.oauth2Bind.title')}
            subtitle={t('pages.auth.oauth2Bind.subtitle')}
        >
            <Result
                status={status === 'success' ? 'success' : 'error'}
                title={status === 'success'
                    ? t('pages.auth.oauth2Bind.successTitle')
                    : t('pages.auth.oauth2Bind.failedTitle')
                }
                subTitle={status === 'error' ? errorMessage : undefined}
                extra={
                    <Button type="primary" onClick={handleReturn}>
                        {t('pages.auth.oauth2Bind.return')}
                    </Button>
                }
            />
        </AuthCardLayout>
    );
}
