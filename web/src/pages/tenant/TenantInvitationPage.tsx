import {Card} from 'antd';
import {useNavigate, useSearchParams} from "react-router-dom";
import {TenantInvitationFlow} from "../../components/tenant/TenantInvitationFlow.tsx";
import {useTranslation} from "react-i18next";
import {buildDocumentTitle, ProjectDisplayName} from "@/global/global-settings.ts";
import {useEffect} from "react";

export function TenantInvitationPage() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const code = searchParams.get("code");

    useEffect(() => {
        document.title = buildDocumentTitle(t('pages.tenantInvitation.title'));
    }, [t]);

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="w-full max-w-[480px]">
                <Card
                    className="border-none shadow-lg rounded-2xl overflow-hidden"
                    styles={{body: {padding: '40px 32px'}}}
                >
                    <TenantInvitationFlow
                        initialCode={code || undefined}
                        onFinish={() => navigate('/')}
                    />
                </Card>

                <div className="mt-8 text-center text-gray-400 text-sm">
                    <span className="font-bold">{ProjectDisplayName}</span>
                </div>
            </div>
        </div>
    );
}

export default TenantInvitationPage;
