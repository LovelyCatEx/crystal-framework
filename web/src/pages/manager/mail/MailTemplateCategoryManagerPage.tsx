import {Col, Form, Input, Row} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    MailTemplateCategoryManagerController,
    type ManagerCreateMailTemplateCategoryDTO,
    type ManagerReadMailTemplateCategoryDTO
} from "@/api/mail-template-category.api.ts";
import {useRef} from "react";
import {
    MAIL_TEMPLATE_CATEGORY_MANAGER_TABLE_COLUMNS
} from "@/components/columns/MailTemplateCategoryEntityColumns.tsx";

export function MailTemplateCategoryManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="邮件模板分类"
            title="邮件模板分类管理"
            subtitle="管理邮件模板分类"
            columns={MAIL_TEMPLATE_CATEGORY_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="名称" rules={[{ required: true }, { max: 128, message: '名称长度不能超过128个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="分类名称" maxLength={128} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述" rules={[{ max: 512, message: '描述长度不能超过512个字符' }]}>
                        <Input.TextArea 
                            className="w-full rounded-lg" 
                            placeholder="分类描述" 
                            rows={4}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadMailTemplateCategoryDTO) => {
                return (await MailTemplateCategoryManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await MailTemplateCategoryManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await MailTemplateCategoryManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await MailTemplateCategoryManagerController.create(props as ManagerCreateMailTemplateCategoryDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
