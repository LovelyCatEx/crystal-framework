import React, {type JSX} from "react";

export interface MenuGroup {
    name: string;
    icon: JSX.Element | React.ReactNode;
    label: string;
}

export interface MenuItem {
    path: string;
    icon: JSX.Element | React.ReactNode;
    label: string;
    page?: JSX.Element | React.ReactNode;
    group?: string;
}