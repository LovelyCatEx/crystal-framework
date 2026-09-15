/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import * as React from "react";
import {forwardRef} from "react";
import {ContextMenuContainer} from "@/rete/ui/menu/ContextMenuContainer.tsx";


export const ContextMenuSubItem = (additionalProps?: React.HTMLProps<HTMLDivElement>) => {
  const ContextMenuContainerComponent = ContextMenuContainer(additionalProps)

  return forwardRef<
    HTMLDivElement,
    React.HTMLProps<HTMLDivElement>
  >((props, ref) => {
    return <ContextMenuContainerComponent ref={ref}>
      {props.children}
    </ContextMenuContainerComponent>;
  });
}