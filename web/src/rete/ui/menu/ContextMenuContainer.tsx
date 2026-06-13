/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {forwardRef} from "react";
import * as React from "react";

export const ContextMenuContainer = (additionalProps?: React.HTMLProps<HTMLDivElement>) => {
  return forwardRef<
    HTMLDivElement,
    React.HTMLProps<HTMLDivElement>
  >((props, ref) => {
    return <div
      ref={ref}
      {...props}
      className={(additionalProps?.className ?? "") + " " + (props.className ?? "")}
      style={{ ...props.style, ...additionalProps?.style }}
    >
      {props.children}
    </div>;
  });
}