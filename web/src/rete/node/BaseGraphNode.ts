/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {ClassicPreset} from "rete";
import {BaseGraphSocket} from "../socket/BaseGraphSocket.ts";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

export abstract class BaseGraphNode<
  S extends BaseGraphSocket,
  C extends BaseGraphControl<S>
> extends ClassicPreset.Node<
  Record<string, NonNullable<S>>,
  Record<string, NonNullable<S>>,
  Record<string, NonNullable<C>>
> {
  public width: number = 300;
  public height: number = 180;

  protected constructor(nodeType: string) {
    super(nodeType);
  }

  public confirmSize(width: number, height: number) {
    this.width = width;
    this.height = height;
  }

  public addInputSocket(label: string, socket: S, displayName?: string) {
    this.addInput(label, new ClassicPreset.Input<S>(socket, displayName))
  }

  public removeInputSocket(label: string) {
    for (const input in this.inputs) {
      if (input == label) {
        this.removeInput(input);
      }
    }
  }

  public clearInputs() {
    for (const input in this.inputs) {
      this.removeInput(input);
    }
  }

  public addOutputSocket(label: string, socket: S, displayName?: string) {
    this.addOutput(label, new ClassicPreset.Output<S>(socket, displayName))
  }

  public removeOutputSocket(label: string) {
    for (const output in this.outputs) {
      if (output == label) {
        this.removeOutput(output);
      }
    }
  }

  public clearOutputs() {
    for (const output in this.outputs) {
      this.removeOutput(output);
    }
  }

  public addInputSocketControl(portLabel: string, control: C) {
    this.addControl(`in::${portLabel}`, control);
  }

  public addOutputSocketControl(portLabel: string, control: C) {
    this.addControl(`out::${portLabel}`, control);
  }

  public getInputSocketControl(portLabel: string): C | undefined {
    return this.controls[`in::${portLabel}`];
  }

  public getOutputSocketControl(portLabel: string): C | undefined {
    return this.controls[`out::${portLabel}`];
  }

  public clearInputSocketControls() {
    Object.entries(this.controls).forEach(([controlKey, _]) => {
      if (controlKey.startsWith('in::')) {
        this.removeControl(controlKey);
      }
    });
  }

  public clearOutputSocketControls() {
    Object.entries(this.controls).forEach(([controlKey, _]) => {
      if (controlKey.startsWith('out::')) {
        this.removeControl(controlKey);
      }
    });
  }
}