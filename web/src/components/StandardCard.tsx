import {Card} from "antd";
import type {HTMLAttributes} from "react";

export function StandardCard(props: HTMLAttributes<HTMLDivElement>) {
    return (
        <Card {...props} className={`border-none shadow-sm rounded-2xl overflow-hidden ${props.className}`}>
            {props.children}
        </Card>
    )
}