import {Button, theme, Typography} from 'antd';
import {useNavigate} from 'react-router-dom';
import {HomeOutlined, QuestionCircleOutlined} from "@ant-design/icons";
import {useEffect} from "react";
import {buildDocumentTitle} from "@/global/global-settings.ts";

const { Title } = Typography;
const { useToken } = theme;

export function NotFoundPage() {
    const navigate = useNavigate();
    const { token } = useToken();

    useEffect(() => {
        document.title = buildDocumentTitle('404')
    }, []);

    return (
        <div className="min-h-screen flex items-center justify-center p-4 font-sans antialiased overflow-hidden"
             style={{ backgroundColor: token.colorBgLayout }}>
            <div className="fixed top-0 left-0 w-full h-full overflow-hidden pointer-events-none -z-10">
                <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full blur-3xl animate-pulse"
                     style={{ backgroundColor: `${token.colorPrimary}20` }}></div>
                <div className="absolute bottom-[-10%] left-[-10%] w-[400px] h-[400px] rounded-full blur-3xl animate-pulse"
                     style={{ backgroundColor: `${token.colorPrimary}15`, animationDelay: '1s' }}></div>
            </div>

            <div className="w-full max-w-[520px] text-center">
                <div className="relative mb-8 animate-in fade-in slide-in-from-bottom-8 duration-700">
                    <div className="text-[140px] font-black leading-none select-none"
                         style={{ color: `${token.colorPrimary}15` }}>
                        404
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center">
                        <div className="w-32 h-32 bg-white rounded-[32px] shadow-2xl flex items-center justify-center animate-bounce duration-[3000ms]"
                             style={{ boxShadow: `0 25px 50px -12px ${token.colorPrimary}30` }}>
                            <div className="relative">
                                <QuestionCircleOutlined className="text-6xl" style={{ color: token.colorPrimary }} />
                                <div className="absolute -top-2 -right-2 w-4 h-4 rounded-full animate-ping"
                                     style={{ backgroundColor: token.colorError }}></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="animate-in fade-in slide-in-from-bottom-4 duration-700 delay-150">
                    <Title level={2} className="!font-bold !mb-2" style={{ color: token.colorTextHeading }}>
                        哎呀！路径丢失了
                    </Title>
                    <p className="text-lg mb-10 max-w-[360px] mx-auto" style={{ color: token.colorTextSecondary }}>
                        您访问的页面可能已被移动、删除或不存在。
                    </p>

                    <Button
                        type="primary"
                        size="large"
                        block
                        icon={<HomeOutlined />}
                        className="mt-4 h-14 rounded-2xl shadow-lg font-bold flex items-center justify-center"
                        style={{ boxShadow: `0 10px 15px -3px ${token.colorPrimary}40` }}
                        onClick={() => navigate('/')}
                    >
                        返回首页
                    </Button>
                </div>
            </div>
        </div>
    );
}

export default NotFoundPage;
