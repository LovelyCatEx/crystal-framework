import type {Key, ReactNode} from "react";
import {Card, Col, Row, theme, Tree} from "antd";
import type {DataNode} from "antd/es/tree";

export interface TreeDetailLayoutTreeProps {
    /** Header content for the left card (usually a translated string wrapped in a `<span>`). */
    title: ReactNode;
    /** antd tree data — typically produced by `useEntityTree().treeData`. */
    treeData: DataNode[];
    /** Currently selected key, or `null` when nothing is selected. */
    selectedKey: string | null;
    /** Whether the tree card is in its loading state. */
    loading: boolean;
    /** Called with the newly selected key (`null` when deselected). */
    onSelect: (key: string | null) => void;
    /**
     * Rendered inside the tree card when `treeData` is empty. Typical use: an empty-
     * state hint plus a "create first item" call to action.
     */
    emptyContent?: ReactNode;
}

export interface TreeDetailLayoutDetailProps {
    /** Rendered on the right when a tree node is selected. */
    content: ReactNode;
    /** Rendered on the right when nothing is selected (e.g. a hint to pick a node). */
    emptyContent: ReactNode;
}

export interface TreeDetailLayoutProps {
    tree: TreeDetailLayoutTreeProps;
    detail: TreeDetailLayoutDetailProps;
    /** Left column span at xl breakpoint. Default: 5. */
    leftSpan?: number;
    /** Right column span at xl breakpoint. Default: 19. */
    rightSpan?: number;
}

/**
 * Pure layout shell for the recurring "tree on the left, detail on the right"
 * pattern used by tenant department pages. Handles nothing beyond rendering — the
 * caller supplies data, selection state, and the detail body.
 */
export function TreeDetailLayout(props: TreeDetailLayoutProps) {
    const {tree, detail, leftSpan = 5, rightSpan = 19} = props;
    const {token} = theme.useToken();

    const hasTreeData = tree.treeData.length > 0;
    const selectedKeys: Key[] = tree.selectedKey ? [tree.selectedKey] : [];

    return (
        <Row gutter={24} className="mt-4">
            <Col xs={24} xl={leftSpan} className="mb-4 xl:mb-0">
                <Card
                    title={<span style={{color: token.colorTextHeading}}>{tree.title}</span>}
                    className="border-none shadow-sm rounded-2xl overflow-hidden"
                    loading={tree.loading}
                >
                    {hasTreeData ? (
                        <Tree
                            treeData={tree.treeData}
                            onSelect={(keys) => tree.onSelect((keys[0] as string | undefined) ?? null)}
                            selectedKeys={selectedKeys}
                            defaultExpandAll
                            blockNode
                            showLine
                        />
                    ) : tree.emptyContent}
                </Card>
            </Col>
            <Col xs={24} xl={rightSpan}>
                {tree.selectedKey ? detail.content : (
                    <Card className="border-none shadow-sm rounded-2xl overflow-hidden h-full flex items-center justify-center">
                        {detail.emptyContent}
                    </Card>
                )}
            </Col>
        </Row>
    );
}
