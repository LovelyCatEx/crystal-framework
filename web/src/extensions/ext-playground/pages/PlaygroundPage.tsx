import {Card, Typography} from "antd";
import {ToolOutlined} from "@ant-design/icons";
import {useTranslation, Trans} from "react-i18next";

const {Title, Paragraph} = Typography;

export function PlaygroundPage() {
    const {t} = useTranslation();

    return (
        <div style={{padding: 24}}>
            <Card>
                <Title level={3}>
                    <ToolOutlined /> {t('pages.extPlayground.title')}
                </Title>
                <Paragraph>
                    <Trans
                        i18nKey="pages.extPlayground.description"
                        components={{code: <code />}}
                    />
                </Paragraph>
                <Paragraph type="secondary">
                    {t('pages.extPlayground.secondary')}
                </Paragraph>
            </Card>
        </div>
    );
}
