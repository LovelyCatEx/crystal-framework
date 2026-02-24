import React, {type JSX} from "react";

export interface ActionBarComponentProps {
    title: string;
    subtitle?: string;
    titleActions?: JSX.Element | React.ReactNode;
}

export function ActionBarComponent(props: ActionBarComponentProps) {
    return (
        <>
            <div className="mb-8 flex justify-between items-end">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800 mb-2">{props.title}</h1>
                    {props.subtitle && <p className="text-gray-500 mt-1">{props.subtitle}</p>}
                </div>

                {props.titleActions}
            </div>
        </>
    )
}