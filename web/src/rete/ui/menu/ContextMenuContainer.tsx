/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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