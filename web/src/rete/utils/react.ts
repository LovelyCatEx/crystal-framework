/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import * as React from "react";
import {useLayoutEffect} from "react";
import type {BaseGraphNode} from "@/rete/node/BaseGraphNode.ts";

export function useNodeSize(
  ref: React.RefObject<HTMLElement | null | undefined> | null | undefined,
  node: BaseGraphNode<any, any>,
  emit: any
) {
  useLayoutEffect(() => {
    if (!ref) return;

    const el = ref.current;
    if (!el) return;

    const observer = new ResizeObserver(() => {
      const width = el.offsetWidth;
      const height = el.offsetHeight;
      if (width > 0 && height > 0) {
        node.confirmSize(width, height);
      }
    });

    observer.observe(el);

    return () => observer.disconnect();
  }, [ref, node, emit]);
}
