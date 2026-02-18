import React, {type JSX} from "react";

export interface MenuItem {
    path: string;
    icon: JSX.Element | React.ReactNode;
    label: string;
    page?: JSX.Element | React.ReactNode;
}