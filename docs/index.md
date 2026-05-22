---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "CrystalFramework"
  text: "Docs"
  tagline: v1.1.0
  actions:
    - theme: brand
      text: 开发文档
      link: /develop-guide.md
    - theme: alt
      text: 部署文档
      link: /deploy-guide.md

features:
  - title: 认证授权体系
    details: 支持 OAuth2 登录（GitHub、Google、QQ）、JWT 令牌管理
  - title: RBAC 权限管理
    details: 灵活的角色权限控制，支持系统级和租户级权限
  - title: 多租户隔离
    details: 完善的租户隔离机制，支持租户级权限和数据隔离
  - title: 智能缓存机制
    details: 基于 Redis Cache 的实体缓存与 Session 管理，自动失效策略
  - title: 邮件服务模块
    details: 支持邮件模板管理、多类型邮件发送
  - title: 资源管理模块
    details: 文件存储抽象，支持阿里云 OSS、腾讯云 COS 等多种存储提供商
  - title: 审计日志
    details: 完整的操作记录追踪，便于安全审计
  - title: 接口加密
    details: 基于 RSA+AES 实现接口数据加密，防护中间人攻击
---

