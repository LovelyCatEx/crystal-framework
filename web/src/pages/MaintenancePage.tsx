import {theme, Typography} from 'antd';
import {ToolOutlined} from "@ant-design/icons";
import {useEffect} from "react";
import {buildDocumentTitle} from "@/global/global-settings.ts";
import {useTranslation} from "react-i18next";

const {Title, Paragraph} = Typography;
const {useToken} = theme;

export function MaintenancePage() {
    const {token} = useToken();
    const {t} = useTranslation();

    useEffect(() => {
        document.title = buildDocumentTitle(t('pages.maintenance.documentTitle'));
    }, [t]);

    return (
        <div className="min-h-screen flex items-center justify-center p-4 font-sans antialiased overflow-hidden"
             style={{backgroundColor: token.colorBgLayout}}>
            {/* Background decorations */}
            <div className="fixed top-0 left-0 w-full h-full overflow-hidden pointer-events-none -z-10">
                <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full blur-3xl animate-pulse"
                     style={{backgroundColor: `${token.colorWarning}20`}}></div>
                <div className="absolute bottom-[-10%] left-[-10%] w-[400px] h-[400px] rounded-full blur-3xl animate-pulse"
                     style={{backgroundColor: `${token.colorWarning}15`, animationDelay: '1s'}}></div>
            </div>

            <div className="w-full max-w-[520px] text-center">
                {/* Icon area */}
                <div className="relative mb-8">
                    <div className="text-[140px] font-black leading-none select-none"
                         style={{color: `${token.colorWarning}15`}}>
                        503
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center">
                        <div className="w-32 h-32 rounded-[32px] shadow-2xl flex items-center justify-center"
                             style={{
                                 backgroundColor: token.colorBgContainer,
                                 boxShadow: `0 25px 50px -12px ${token.colorWarning}30`
                             }}>
                            <ToolOutlined className="text-6xl" style={{color: token.colorWarning}}/>
                        </div>
                    </div>
                </div>

                {/* Text area */}
                <div>
                    <Title level={2} className="!font-bold !mb-2" style={{color: token.colorTextHeading}}>
                        {t('pages.maintenance.title')}
                    </Title>
                    <Paragraph className="text-lg !mb-4 max-w-[360px] mx-auto"
                               style={{color: token.colorTextSecondary}}>
                        {t('pages.maintenance.description')}
                    </Paragraph>
                    <Paragraph className="text-sm max-w-[360px] mx-auto"
                               style={{color: token.colorTextTertiary}}>
                        {t('pages.maintenance.hint')}
                    </Paragraph>
                </div>
            </div>
        </div>
    );
}

export default MaintenancePage;
