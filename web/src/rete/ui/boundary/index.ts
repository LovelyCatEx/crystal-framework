/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import { NodeEditor } from "rete";
import { AreaPlugin, AreaExtensions } from "rete-area-plugin";

function getWeight(value: number, padding: number) {
  return Math.min(1, -Math.min(0, value / padding - 1));
}

export function getFrameWeight(
  x: number,
  y: number,
  frame: DOMRect,
  padding: number
) {
  const top = getWeight(y - frame.top, padding);
  const bottom = getWeight(frame.bottom - y, padding);
  const left = getWeight(x - frame.left, padding);
  const right = getWeight(frame.right - x, padding);

  return {
    top,
    bottom,
    left,
    right
  };
}

export function watchPointerMove() {
  let moveEvent: PointerEvent | null = null;

  function pointermove(e: PointerEvent) {
    moveEvent = e;
  }

  window.addEventListener("pointermove", pointermove);

  return {
    getEvent() {
      if (!moveEvent) throw new Error("no event captured");
      return moveEvent;
    },
    destroy() {
      window.removeEventListener("pointermove", pointermove);
    }
  };
}

export function animate(handle: () => void | Promise<void>) {
  let id = 0;

  function start() {
    id = requestAnimationFrame(async () => {
      try {
        await handle();
      } catch (e) {
        console.error(e);
      } finally {
        start();
      }
    });
  }
  function stop() {
    cancelAnimationFrame(id);
  }

  return {
    start,
    stop
  };
}

type Props = {
  editor: NodeEditor<any>;
  area: AreaPlugin<any, any>;
  selector: AreaExtensions.Selector<any>;
  intensity?: number;
  padding?: number;
};

export function setupPanningBoundary(props: Props) {
  const selector = props.selector;
  const padding = props.padding ?? 30;
  const intensity = props.intensity ?? 2;
  const area = props.area;
  const editor = props.editor;
  const pointermove = watchPointerMove();
  const ticker = animate(async () => {
    const { clientX, clientY, pageX, pageY } = pointermove.getEvent();
    const weights = getFrameWeight(
      clientX,
      clientY,
      area.container.getBoundingClientRect(),
      padding
    );
    const velocity = {
      x: (weights.left - weights.right) * intensity,
      y: (weights.top - weights.bottom) * intensity,
    };

    const pickedNode = editor
      .getNodes()
      .find((n) => selector.isPicked({ label: "node", id: n.id }));
    const view = pickedNode && area.nodeViews.get(pickedNode.id);

    if (!view) return;

    const { dragHandler, position } = view;

    (dragHandler as any).pointerStart = {
      x: pageX + velocity.x,
      y: pageY + velocity.y,
    };
    (dragHandler as any).startPosition = {
      ...(dragHandler as any).config.getCurrentPosition(),
    };

    const { transform } = area.area;
    const x = position.x - velocity.x / transform.k;
    const y = position.y - velocity.y / transform.k;

    await Promise.all([
      area.area.translate(transform.x + velocity.x, transform.y + velocity.y),
      area.translate(pickedNode.id, { x, y }),
    ]);
  });

  area.addPipe((context) => {
    if (context.type === "nodepicked") ticker.start();
    if (context.type === "nodedragged") ticker.stop();
    return context;
  });

  return {
    destroy() {
      pointermove.destroy();
    },
  };
}