import {Button, message, Tooltip} from "antd";
import {useState, type ReactNode} from "react";
import {CheckOutlined, CopyOutlined} from "@ant-design/icons";

export function CopyableToolTip(props: { title: string | ReactNode, children?: ReactNode }) {
    const [copied, setCopied] = useState(false);

    return <Tooltip title={
        <div className="flex flex-row items-center space-x-2">
            <span>{props.title}</span>
            <Button
                className="!border-none !bg-transparent !text-gray-300 hover:!text-white"
                variant="text"
                size="small"
                icon={copied ? <CheckOutlined className="!text-green-500" /> : <CopyOutlined />}
                onClick={(event) => {
                    event.stopPropagation();

                    const content = props.title?.toString()
                    if (!content) {
                        return;
                    }

                    try {
                        void navigator.clipboard.writeText(content);
                        setCopied(true);
                        setTimeout(() => setCopied(false), 3000);
                    } catch (err) {
                        void message.warning('Failed to copy');
                    }
                }}
            />
        </div>
    }>
        {props?.children}
    </Tooltip>
}