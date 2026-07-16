import type {ApprovalFlowGraphSchemes} from "../rete-typs.ts";
import {useRef} from "react";
import {css} from "styled-components";
import {Square} from "lucide-react";
import {type ClassicScheme, Presets, type RenderEmit} from "rete-react-plugin";
import classNames from "classnames";
import {useNodeSize} from "@/rete/utils/react.ts";
import {useApprovalNodeStatus} from "@/components/approval/viewer/ApprovalNodeStatusContext.tsx";
import {ApprovalFlowTaskStatus} from "@/types/approval/approval-enums.ts";
import './base-node-graph-styles.css';
import './sink-graph-node.styles.css';

const { RefSocket } = Presets.classic;

type Props<S extends ClassicScheme> = {
    data: S["Node"];
    styles?: () => ReturnType<typeof css>;
    emit: RenderEmit<S>;
};

export function EndNodeComponent<S extends ApprovalFlowGraphSchemes>(props: Props<S>) {
    const ref = useRef<HTMLDivElement>(null);
    useNodeSize(ref, props.data, props.emit);
    const inputs = Object.entries(props.data.inputs);
    const nodeState = useApprovalNodeStatus(props.data.node.id);

    return (
        <div ref={ref} className={classNames("base-graph-node", "sink-graph-node", {
            "base-graph-node--selected": props.data.selected,
            "base-graph-node--status-approved": nodeState?.status === ApprovalFlowTaskStatus.APPROVED,
            "base-graph-node--status-rejected": nodeState?.status === ApprovalFlowTaskStatus.REJECTED,
        })}>
            <div className="header flex flex-col text-white pl-4 pr-4 pt-2 pb-2">
                <div className="flex flex-row items-center space-x-2">
                    <Square size="20" />
                    <div className="title">{props.data.node.name}</div>
                </div>
            </div>
            <div className="flex flex-row justify-start p-4">
                {inputs.map(([key, input]) => (
                    input && <div className="input" key={key}>
                        <RefSocket
                            name="input-socket"
                            side="input"
                            socketKey={key}
                            nodeId={props.data.id}
                            emit={props.emit}
                            payload={input.socket}
                        />
                        {input?.label && <span className="ml-2">{input?.label}</span>}
                    </div>
                ))}
            </div>
        </div>
    );
}
