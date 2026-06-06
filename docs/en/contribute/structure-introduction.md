# Project Structure

## .claude

Stores project development guidelines for AI to quickly and standardly complete development tasks

## .devcontainer

Both DevContainer and Docker development methods depend on the container environment in this folder

## .github

Github Actions workflow files

## .run

IDEA Configurations folder

## docker

Docker related files, provides one-click deployment scripts based on source code compilation

## docs

Project documentation site (VitePress), containing contribution guides, development docs, and deployment instructions

## readme

Image assets such as screenshots referenced by the README

## crystal-shared

Shared core module, provides core functionalities such as base repository, authentication, caching, context, and generic responses

## crystal-shared-types

Shared type definition module, provides common type definitions such as system settings and API encryption scopes

## crystal-sdk

SDK module, enables developers to quickly integrate this framework with zero code intrusion

## crystal-audit

Audit log module, provides operation audit, session tracking, and audit log management functionalities

## crystal-schedule

Task scheduling module, provides scheduled task registration, execution, and management functionalities

## crystal-resource

Resource management module, provides file storage, multi-storage provider support (local/OSS/COS), and resource routing functionalities

## crystal-encrypt

Encryption module, provides API response data encryption functionality

## crystal-mail

Mail module, provides mail sending, template management, and mail log functionalities

## crystal-message-channel

Message channel module, provides a unified abstraction and routing for multi-channel message delivery (mail, Lark, etc.)

## crystal-monitor

Monitoring module, provides system runtime status collection and dashboard statistics functionalities

## crystal-user

User module, provides user account, profile, and third-party OAuth account management functionalities

## crystal-rbac

Authorization module, provides management of system-level and tenant-level roles, permissions, and their relations

## crystal-auth

Authentication module, provides password login, OAuth2 login, JWT issuance, and security filtering functionalities

## crystal-tenant

Tenant module, provides multi-tenant core, members, departments, invitations, benefit tiers, and tenant settings functionalities

## crystal-system

System module, provides system initialization, announcements, system settings, and dashboard aggregation functionalities

## crystal-starter

Backend startup module, integrates all functional modules and provides application entry point

## web

Frontend project folder
