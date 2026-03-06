import {Col, Form, Input, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    MailTemplateTypeManagerController,
    type ManagerCreateMailTemplateTypeDTO,
    type ManagerReadMailTemplateTypeDTO
} from "../../../api/mail-template-type.api.ts";
import {useEffect, useRef, useState} from "react";
import type {MailTemplateCategory} from "../../../types/mail.types.ts";
import {MAIL_TEMPLATE_TYPE_MANAGER_TABLE_COLUMNS} from "../../../components/columns/MailTemplateTypeEntityColumns.tsx";
import {MailTemplateCategoryManagerController} from "../../../api/mail-template-category.api.ts";
import {JsonEditor} from "../../../components/JsonEditor.tsx";

export function MailTemplateTypeManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [categories, setCategories] = useState<MailTemplateCategory[]>([]);

    useEffect(() => {
        MailTemplateCategoryManagerController.list().then((res) => {
            setCategories(res.data || []);
        });
    }, []);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="邮件模板类型"
            title="邮件模板类型管理"
            subtitle="管理邮件模板类型"
            columns={MAIL_TEMPLATE_TYPE_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="类型名称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="categoryId" label="分类" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择分类"
                                    options={categories.map((cat) => ({
                                        label: cat.name,
                                        value: cat.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea 
                            className="w-full rounded-lg" 
                            placeholder="类型描述" 
                            rows={3}
                        />
                    </Form.Item>
                    <Form.Item name="variables" label="变量(JSON格式)" rules={[{ required: true }]}>
                        <JsonEditor placeholder='{"username": "用户名", "code": "验证码"}' />
                    </Form.Item>
                    <Form.Item name="allowMultiple" label="允许多个" valuePropName="checked">
                        <Switch />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadMailTemplateTypeDTO) => {
                return (await MailTemplateTypeManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await MailTemplateTypeManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await MailTemplateTypeManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await MailTemplateTypeManagerController.create(props as ManagerCreateMailTemplateTypeDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
