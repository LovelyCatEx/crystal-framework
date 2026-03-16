import React, {type JSX} from "react";
import {theme} from "antd";

const { useToken } = theme;

export interface ActionBarComponentProps {
    title: string;
    subtitle?: string;
    titleActions?: JSX.Element | React.ReactNode;
}

export function ActionBarComponent(props: ActionBarComponentProps) {
    const { token } = useToken();

    return (
        <>
            <div className="mb-8 flex justify-between items-end gap-4 flex-wrap">
                <div>
                    <h1
                        className="text-2xl font-bold"
                        style={{ color: token.colorTextHeading }}
                    >
                        {props.title}
                    </h1>
                    {props.subtitle && (
                        <p
                            className="mt-2"
                            style={{ color: token.colorTextSecondary }}
                        >
                            {props.subtitle}
                        </p>
                    )}
                </div>

                {props.titleActions}
            </div>
        </>
    )
}