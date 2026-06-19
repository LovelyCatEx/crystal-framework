/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import * as React from "react";
import {forwardRef} from "react";
import type {Item} from "rete-react-plugin/_types/presets/context-menu/types";
import {ChevronRight} from "lucide-react";

export const ContextMenuItem = (
  item: Item,
  additionalProps?: React.HTMLProps<HTMLDivElement>,
  deleteItemProps?: React.HTMLProps<HTMLDivElement>
) => {
  return forwardRef<
    HTMLDivElement,
    React.HTMLProps<HTMLDivElement>
  >((props, ref) => {
    const filteredChildren = React.Children.toArray(props.children).filter(
      (child) => React.isValidElement(child)
    );

    const normalDiv = <div
      ref={ref}
      {...props}
      className={(additionalProps?.className ?? "") + " " + (props.className ?? "")}
      style={{ ...props.style, ...additionalProps?.style }}
    >
      <span>{item.label}</span>
      {(item.subitems?.length || 0) > 0 && <ChevronRight size="20" />}
      {(item.subitems?.length || 0) > 0 && (
        <div className="absolute left-full hidden group-hover:block hover:block z-50 ml-[-.5rem]">
          {filteredChildren}
        </div>
      )}
    </div>;

    const deleteDiv = <div
      ref={ref}
      {...props}
      className={(props.className ?? "") + " " + (deleteItemProps?.className ?? "")}
      style={{ ...props.style, ...deleteItemProps?.style }}
    >
      <p>{item.label}</p>
    </div>

    return item.key == 'delete' ? deleteDiv : normalDiv;
  });
}