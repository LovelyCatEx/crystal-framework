import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    TenantTireTypeManagerController,
    type ManagerCreateTenantTireTypeDTO,
    type ManagerReadTenantTireTypeDTO
} from "@/api/tenant-tire-type.api.ts";
import {useRef} from "react";
import {TENANT_TIRE_TYPE_MANAGER_TABLE_COLUMNS} from "@/components/columns/TenantTireTypeEntityColumns.tsx";

export function TenantTireTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="套餐类型"
            title="套餐类型管理"
            subtitle="管理系统套餐类型"
            columns={TENANT_TIRE_TYPE_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="名称" rules={[{ required: true }, { max: 32, message: '名称长度不能超过32个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="套餐类型名称" maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述" rules={[{ max: 512, message: '描述长度不能超过512个字符' }]}>
                        <Input.TextArea 
                            className="w-full rounded-lg" 
                            placeholder="套餐类型描述" 
                            rows={4}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadTenantTireTypeDTO) => {
                return (await TenantTireTypeManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantTireTypeManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await TenantTireTypeManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await TenantTireTypeManagerController.create(props as ManagerCreateTenantTireTypeDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
