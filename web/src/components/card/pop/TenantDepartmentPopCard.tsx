import {Card, Descriptions, Popover, Spin, Tag} from "antd";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import {useSWRComposition} from "@/compositions/swr.ts";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";

interface TenantDepartmentPopCardProps {
    departmentId: string;
}

export function TenantDepartmentPopCard({ departmentId }: TenantDepartmentPopCardProps) {
    const { data: department, isLoading } = useSWRComposition<TenantDepartment | null>(
        `tenant-department-card-${departmentId}`,
        async () => {
            return await TenantDepartmentManagerController.getById(departmentId);
        }
    );

    const { data: parentDepartment } = useSWRComposition<TenantDepartment | null>(
        department?.parentId ? `tenant-department-card-${department.parentId}` : undefined,
        async () => {
            if (!department?.parentId) return null;
            return await TenantDepartmentManagerController.getById(department.parentId);
        }
    );

    if (isLoading) {
        return (
            <Card size="small" className="w-64">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!department) {
        return (
            <Card size="small" className="w-64">
                <div className="text-center py-4 text-gray-400">
                    未找到部门信息
                </div>
            </Card>
        );
    }

    return (
        <Card
            size="small"
            className="w-72"
            title={
                <div className="flex items-center gap-2 pt-2 pb-2">
                    <CopyableToolTip title={department.name}>
                        <span className="font-bold">{department.name}</span>
                    </CopyableToolTip>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={department.id}>
                        <Tag color="blue" className="text-xs">{department.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label="描述">
                    <span className="text-gray-600">{department.description || '-'}</span>
                </Descriptions.Item>
                <Descriptions.Item label="父部门">
                    {department.parentId ? (
                        <Popover
                            content={<TenantDepartmentPopCard departmentId={department.parentId} />}
                            placement="right"
                            trigger="hover"
                        >
                            <CopyableToolTip title={department.parentId}>
                                <Tag color="orange" className="text-xs cursor-pointer">
                                    {parentDepartment?.name || department.parentId}
                                </Tag>
                            </CopyableToolTip>
                        </Popover>
                    ) : (
                        '-'
                    )}
                </Descriptions.Item>
                <Descriptions.Item label="租户ID">
                    <CopyableToolTip title={department.tenantId}>
                        <Tag color="purple" className="text-xs">{department.tenantId}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
