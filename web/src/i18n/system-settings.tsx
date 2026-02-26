import type {ReactNode} from "react";
import {MailOutlined, SettingOutlined} from "@ant-design/icons";

export const settingsKeyToTranslationMap = new Map<string, string>([
    ['bootstrap.autoCheckRbacTableData', '自动校验 RBAC 表数据'],
    ['mail.smtp.username', '用户名'],
    ['mail.smtp.password', '密码'],
    ['mail.smtp.host', '主机'],
    ['mail.smtp.port', '端口'],
    ['mail.smtp.ssl', '是否启用 SSL'],
    ['mail.smtp.fromEmail', '发件地址'],
])

export const settingsGroupToTranslationMap = new Map<string, {label: string, icon?: ReactNode}>([
    ['bootstrap', { label: '启动设置项', icon: <SettingOutlined /> }],
    ['mail.smtp', { label: 'SMTP 邮件服务', icon: <MailOutlined /> }]
])