---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "CrystalFramework Docs"
  text: ""
  tagline: v1.1.0
  actions:
    - theme: brand
      text: Develop Docs
      link: /en/develop-guide.md
    - theme: alt
      text: Deploy Docs
      link: /en/deploy-guide.md

features:
  - title: Authentication & Authorization
    details: Supports OAuth2 login (GitHub, Google, QQ), JWT token management
  - title: RBAC Permission Management
    details: Flexible role-based access control, supporting system-level and tenant-level permissions
  - title: Multi-Tenant Isolation
    details: Comprehensive tenant isolation mechanism with tenant-level permission and data isolation
  - title: Intelligent Cache Mechanism
    details: Entity cache and Session management based on Redis Cache with automatic invalidation strategy
  - title: Mail Service Module
    details: Supports email template management and multi-type email sending
  - title: Resource Management Module
    details: File storage abstraction supporting Aliyun OSS, Tencent COS and other storage providers
  - title: Audit Logs
    details: Complete operation record tracking for security auditing
  - title: API Encryption
    details: API data encryption based on RSA+AES to prevent man-in-the-middle attacks
---

