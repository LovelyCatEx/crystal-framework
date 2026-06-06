# 项目结构

## .claude

存放项目开发说明以便 AI 能快速且标准化的完成一些开发

## .devcontainer

无论是使用 DevContainer 还是 Docker 都需要依赖此文件夹内的容器环境

## .github

Github Actions 工作流文件

## .run

IDEA Configurations 文件夹

## docker

docker 相关文件，内部提供了基于源码编译的一键部署脚本

## docs

项目文档站点（VitePress），包含贡献指南、开发文档与部署说明

## readme

README 中引用的截图等图片资源

## crystal-shared
共享核心模块，提供基础仓储、认证、缓存、上下文和通用响应等核心功能

## crystal-shared-types
共享类型定义模块，提供系统设置和 API 加密范围等通用类型定义

## crystal-sdk
SDK 模块，以便开发者能够快速且零代码侵入地接入本框架

## crystal-audit
审计日志模块，提供操作审计、会话跟踪和审计日志管理功能

## crystal-schedule
任务调度模块，提供定时任务注册、执行和管理功能

## crystal-resource
资源管理模块，提供文件存储、多存储提供商支持（本地/OSS/COS）和资源路由功能

## crystal-encrypt
加密模块，提供 API 响应数据加密功能

## crystal-mail
邮件模块，提供邮件发送、模板管理和邮件日志功能

## crystal-message-channel
消息渠道模块，提供统一的多渠道消息发送抽象与路由（邮件、飞书等）

## crystal-monitor
监控模块，提供系统运行状态采集与仪表盘统计功能

## crystal-user
用户模块，提供用户账户、个人资料和第三方 OAuth 账户管理功能

## crystal-rbac
权限模块，提供系统级与租户级的角色、权限及其关联关系管理功能

## crystal-auth
认证模块，提供密码登录、OAuth2 登录、JWT 签发与安全过滤功能

## crystal-tenant
租户模块，提供多租户核心、成员、部门、邀请、权益分级和租户设置功能

## crystal-system
系统模块，提供系统初始化、公告、系统设置和仪表盘聚合功能

## crystal-starter
后端启动模块，整合所有功能模块并提供应用程序入口

## web

前端项目文件夹
