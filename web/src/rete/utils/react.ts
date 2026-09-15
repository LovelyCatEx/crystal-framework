/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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
