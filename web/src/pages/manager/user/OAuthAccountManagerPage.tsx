import {Button, Col, Form, Input, Row, Select, Space, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateOAuthAccountDTO,
    type ManagerReadOAuthAccountDTO,
    OAuthAccountManagerController
} from "../../../api/oauth-account.api.ts";
import {useEffect, useRef, useState} from "react";
import {OAUTH_ACCOUNT_MANAGER_TABLE_COLUMNS} from "../../../components/columns/OAuthAccountEntityColumns.tsx";
import {EntitySelectorModal} from "../../../components/EntitySelector.tsx";
import {USER_MANAGER_TABLE_COLUMNS} from "../../../components/columns/UserEntityColumns.tsx";
import {UserManagerController} from "../../../api/user.api.ts";
import type {User} from "../../../types/user.types.ts";
import {UserOutlined} from "@ant-design/icons";

interface UserSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
}

function UserSelector({ value, onChange }: UserSelectorProps) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (value) {
            setLoading(true);
            UserManagerController.getById(value)
                .then((user) => {
                    if (user) {
                        setSelectedUser(user);
                    }
                })
                .finally(() => {
                    setLoading(false);
                });
        } else {
            setSelectedUser(null);
        }
    }, [value]);

    const handleOpenModal = () => {
        setIsModalOpen(true);
    };

    const handleCancel = () => {
        setIsModalOpen(false);
    };

    const handleOk = (selected: User[]) => {
        if (selected.length > 0) {
            const user = selected[0];
            setSelectedUser(user);
            onChange?.(user.id);
        } else {
            setSelectedUser(null);
            onChange?.(null);
        }
        setIsModalOpen(false);
    }

    const handleClear = () => {
        setSelectedUser(null);
        onChange?.(null);
    };

    return (
        <>
            <Space>
                <Button className="h-10" onClick={handleOpenModal}>
                    {loading ? (
                        <Spin size="small" />
                    ) : selectedUser ? (
                        <Space>
                            <UserOutlined />
                            <span>{selectedUser.username} ({selectedUser.email})</span>
                        </Space>
                    ) : value ? (
                        <Space>
                            <UserOutlined />
                            <span>用户ID: {value}</span>
                        </Space>
                    ) : (
                        "选择用户"
                    )}
                </Button>
                {(selectedUser || value) && (
                    <Button type="link" danger onClick={handleClear}>
                        清除
                    </Button>
                )}
            </Space>

            <EntitySelectorModal
                type="radio"
                visible={isModalOpen}
                title="选择系统用户"
                entityName="用户"
                columns={USER_MANAGER_TABLE_COLUMNS}
                query={async (props) => {
                    return (await UserManagerController.query(props)).data!;
                }}
                onCancel={handleCancel}
                onOk={handleOk}
            />
        </>
    );
}

export function OAuthAccountManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPlatform, setFilterPlatform] = useState<number>();

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterPlatform]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="OAuth账号"
            title="OAuth账号管理"
            subtitle="管理系统OAuth账号绑定列表"
            columns={OAUTH_ACCOUNT_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="userId" label="系统用户">
                                <UserSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择平台"
                                    options={[
                                        {
                                            label: 'GITHUB',
                                            value: 0,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="identifier" label="平台标识" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="平台唯一标识" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="nickname" label="昵称">
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="用户昵称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="avatar" label="头像URL">
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="头像链接" />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            }
            query={async (props: ManagerReadOAuthAccountDTO) => {
                return (await OAuthAccountManagerController.query(props)).data!;
            }}
            delete={async (props) => {
                return (await OAuthAccountManagerController.delete(props)).data!;
            }}
            update={async (props) => {
                return (await OAuthAccountManagerController.update(props)).data!;
            }}
            create={async (props) => {
                return (await OAuthAccountManagerController.create(props as ManagerCreateOAuthAccountDTO)).data!;
            }}
            tableActions={[
                {
                    label: <span>平台</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            { value: '0', label: 'GITHUB' },
                        ]}
                        onChange={(value) => setFilterPlatform(Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            platform: filterPlatform === -1 ? undefined : filterPlatform
                        };
                    }
                }
            ]}
        >
        </ManagerPageContainer>
    );
}
