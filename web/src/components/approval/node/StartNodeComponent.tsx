import type {ApprovalFlowGraphSchemes} from "../rete-typs.ts";
import {useRef} from "react";
import {css} from "styled-components";
import {Play} from "lucide-react";
import {type ClassicScheme, Presets, type RenderEmit} from "rete-react-plugin";
import classNames from "classnames";
import './base-node-graph-styles.css';

const { RefSocket } = Presets.classic;

type Props<S extends ClassicScheme> = {
    data: S["Node"];
    styles?: () => ReturnType<typeof css>;
    emit: RenderEmit<S>;
};

export function StartNodeComponent<S extends ApprovalFlowGraphSchemes>(props: Props<S>) {
    const ref = useRef<HTMLDivElement>(null);
    const outputs = Object.entries(props.data.outputs);

    return (
        <div ref={ref} className={"base-graph-node " + classNames({"base-graph-node--selected": props.data.selected})}>
            <div className="header flex flex-col text-white pl-4 pr-4 pt-2 pb-2">
                <div className="flex flex-row items-center space-x-2">
                    <Play size="20" />
                    <div className="title">{props.data.node.name}</div>
                </div>
            </div>
            <div className="flex flex-row justify-end p-4">
                {outputs.map(([key, output]) => (
                    output && <div className="output" key={key}>
                        {output?.label && <span className="mr-2">{output?.label}</span>}
                        <RefSocket
                            name="output-socket"
                            side="output"
                            socketKey={key}
                            nodeId={props.data.id}
                            emit={props.emit}
                            payload={output.socket}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}
