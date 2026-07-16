import type {ApprovalFlowGraphSchemes} from "../rete-typs.ts";
import {useRef} from "react";
import {css} from "styled-components";
import {UserCheck} from "lucide-react";
import {type ClassicScheme, Presets, type RenderEmit} from "rete-react-plugin";
import classNames from "classnames";
import {useNodeSize} from "@/rete/utils/react.ts";
import {UserChipById} from "@/components/UserChipById.tsx";
import {TenantMemberChip} from "@/components/TenantMemberChip.tsx";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {useApprovalEditorContext} from "../ApprovalEditorContext.tsx";
import {getApprovalFlowApproveMode} from "@/i18n/enum-helpers.ts";
import {useApprovalNodeStatus} from "@/components/approval/viewer/ApprovalNodeStatusContext.tsx";
import {ApprovalFlowTaskStatus} from "@/types/approval/approval-enums.ts";
import './base-node-graph-styles.css';
import './action-graph-node.styles.css';

const { RefSocket } = Presets.classic;

type Props<S extends ClassicScheme> = {
    data: S["Node"];
    styles?: () => ReturnType<typeof css>;
    emit: RenderEmit<S>;
};

export function ApprovalNodeComponent<S extends ApprovalFlowGraphSchemes>(props: Props<S>) {
    const ref = useRef<HTMLDivElement>(null);
    useNodeSize(ref, props.data, props.emit);
    const inputs = Object.entries(props.data.inputs);
    const outputs = Object.entries(props.data.outputs);
    const { scope } = useApprovalEditorContext();

    const config = (() => {
        try { return props.data.node.config ? JSON.parse(props.data.node.config) : null; }
        catch { return null; }
    })();

    const userIds: string[] = config?.strategyParams?.userIds ?? [];
    const memberIds: string[] = config?.strategyParams?.memberIds ?? [];
    const approveMode: number | undefined = config?.approveMode;
    const nodeState = useApprovalNodeStatus(props.data.node.id);

    return (
        <div ref={ref} className={classNames("base-graph-node", "action-graph-node", {
            "base-graph-node--selected": props.data.selected,
            "base-graph-node--status-approved": nodeState?.status === ApprovalFlowTaskStatus.APPROVED,
            "base-graph-node--status-rejected": nodeState?.status === ApprovalFlowTaskStatus.REJECTED,
        })}>
            <div className="header flex flex-col text-white pl-4 pr-4 pt-2 pb-2">
                <div className="flex flex-row items-center space-x-2">
                    <UserCheck size="20" />
                    <div className="title">{props.data.node.name} ({props.data.node.nodeKey})</div>
                </div>
            </div>
            <div className="flex flex-row justify-between p-4 space-x-4">
                <div>
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
                <div>
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
            {(userIds.length > 0 || memberIds.length > 0) && (
                <div className="px-4 pb-3">
                    <div className="flex flex-wrap gap-1" style={{ maxWidth: 260 }}>
                        {scope === ResourceScope.TENANT
                            ? memberIds.map(id => <TenantMemberChip key={id} memberId={id} />)
                            : userIds.map(id => <UserChipById key={id} userId={id} />)
                        }
                    </div>
                    {approveMode !== undefined && (
                        <span className="inline-block mt-4 px-3 py-1 text-sm rounded-md bg-slate-700/70 text-slate-100 border border-slate-500/40">
                            {getApprovalFlowApproveMode(approveMode)}
                        </span>
                    )}
                </div>
            )}
        </div>
    );
}
