import {Button, message, Tooltip} from "antd";
import type {ReactNode} from "react";
import {CopyOutlined} from "@ant-design/icons";

export function CopyableToolTip(props: { title: string | ReactNode, children?: ReactNode }) {
    return <Tooltip title={
        <div className="flex flex-row items-center space-x-2">
            <span>{props.title}</span>
            <Button
                className="!bg-gray-800 !text-gray-300 !border-gray-600 hover:!bg-gray-700 hover:!border-gray-500 hover:!text-white"
                variant="text"
                size="small"
                shape="circle"
                icon={<CopyOutlined />}
                onClick={(event) => {
                    event.stopPropagation();

                    const content = props.title?.toString()
                    if (!content) {
                        return;
                    }

                    try {
                        void navigator.clipboard.writeText(content);
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