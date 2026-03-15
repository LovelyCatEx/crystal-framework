import React, {type JSX} from "react";

export interface ActionBarComponentProps {
    title: string;
    subtitle?: string;
    titleActions?: JSX.Element | React.ReactNode;
}

export function ActionBarComponent(props: ActionBarComponentProps) {
    return (
        <>
            <div className="mb-8 flex justify-between items-end gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">{props.title}</h1>
                    {props.subtitle && <p className="text-gray-500 mt-2">{props.subtitle}</p>}
                </div>

                {props.titleActions}
            </div>
        </>
    )
}