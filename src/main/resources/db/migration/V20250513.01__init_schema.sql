-- Crystal Framework initial schema
-- Migrated from docker/postgres/init/init.sql

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;
SET default_tablespace = '';
SET default_table_access_method = heap;

-- file_resources
CREATE TABLE IF NOT EXISTS public.file_resources (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    type integer NOT NULL,
    file_name character varying(256) NOT NULL,
    file_extension character varying(64) NOT NULL,
    md5 character varying(32) NOT NULL,
    file_size bigint NOT NULL,
    storage_provider_id bigint NOT NULL,
    object_key character varying(256) NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- mail_template_categories
CREATE TABLE IF NOT EXISTS public.mail_template_categories (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- mail_template_types
CREATE TABLE IF NOT EXISTS public.mail_template_types (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    variables text NOT NULL,
    category_id bigint NOT NULL,
    allow_multiple boolean NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- mail_templates
CREATE TABLE IF NOT EXISTS public.mail_templates (
    id bigint NOT NULL,
    type_id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    title character varying(512) NOT NULL,
    content text NOT NULL,
    active boolean NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- oauth_accounts
CREATE TABLE IF NOT EXISTS public.oauth_accounts (
    id bigint NOT NULL,
    user_id bigint,
    platform integer NOT NULL,
    identifier character varying(256) NOT NULL,
    nickname character varying(128),
    avatar character varying(256),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    email character varying(256)
);

-- storage_providers
CREATE TABLE IF NOT EXISTS public.storage_providers (
    id bigint NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(512),
    type integer NOT NULL,
    base_url character varying(256) NOT NULL,
    properties text NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    active boolean DEFAULT true NOT NULL
);

-- system_settings
CREATE TABLE IF NOT EXISTS public.system_settings (
    id bigint NOT NULL,
    config_key character varying(256) NOT NULL,
    config_value text,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_department_member_relations
CREATE TABLE IF NOT EXISTS public.tenant_department_member_relations (
    id bigint NOT NULL,
    department_id bigint NOT NULL,
    member_id bigint NOT NULL,
    role_type integer NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_departments
CREATE TABLE IF NOT EXISTS public.tenant_departments (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(512),
    parent_id bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_invitation_records
CREATE TABLE IF NOT EXISTS public.tenant_invitation_records (
    id bigint NOT NULL,
    invitation_id bigint NOT NULL,
    used_user_id bigint NOT NULL,
    real_name character varying(64) NOT NULL,
    phone_number character varying(32) NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_invitations
CREATE TABLE IF NOT EXISTS public.tenant_invitations (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    creator_member_id bigint NOT NULL,
    department_id bigint,
    invitation_code character varying(16) NOT NULL,
    invitation_count integer NOT NULL,
    expires_time bigint,
    requires_reviewing boolean NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_member_role_relations
CREATE TABLE IF NOT EXISTS public.tenant_member_role_relations (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_members
CREATE TABLE IF NOT EXISTS public.tenant_members (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    member_user_id bigint NOT NULL,
    status integer NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_permissions
CREATE TABLE IF NOT EXISTS public.tenant_permissions (
    id bigint NOT NULL,
    name character varying(256) NOT NULL,
    description character varying(512),
    type integer NOT NULL,
    path character varying(256),
    preserved_1 integer,
    preserved_2 integer,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_role_permission_relations
CREATE TABLE IF NOT EXISTS public.tenant_role_permission_relations (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_roles
CREATE TABLE IF NOT EXISTS public.tenant_roles (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    name character varying(64) NOT NULL,
    description character varying(512),
    parent_id bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_tire_types
CREATE TABLE IF NOT EXISTS public.tenant_tire_types (
    id bigint NOT NULL,
    name character varying(32) NOT NULL,
    description character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenants
CREATE TABLE IF NOT EXISTS public.tenants (
    id bigint NOT NULL,
    owner_user_id bigint NOT NULL,
    name character varying(64) NOT NULL,
    description text,
    icon bigint,
    status integer NOT NULL,
    tire_type_id bigint NOT NULL,
    subscribed_time bigint NOT NULL,
    expires_time bigint NOT NULL,
    contact_name character varying(64) NOT NULL,
    settings text,
    contact_email character varying(256) NOT NULL,
    contact_phone character varying(32) NOT NULL,
    address text NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- user_permissions
CREATE TABLE IF NOT EXISTS public.user_permissions (
    id bigint NOT NULL,
    name character varying(256) NOT NULL,
    description character varying(512),
    type integer NOT NULL,
    path character varying(256),
    preserved_1 integer,
    preserved_2 integer,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- user_role_permission_relations
CREATE TABLE IF NOT EXISTS public.user_role_permission_relations (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- user_role_relations
CREATE TABLE IF NOT EXISTS public.user_role_relations (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- user_roles
CREATE TABLE IF NOT EXISTS public.user_roles (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- users
CREATE TABLE IF NOT EXISTS public.users (
    id bigint NOT NULL,
    username character varying(64) NOT NULL,
    password character varying(256) NOT NULL,
    email character varying(256),
    nickname character varying(32) NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint,
    avatar bigint
);

-- Primary Keys
ALTER TABLE public.file_resources ADD CONSTRAINT file_resources_pk PRIMARY KEY (id);
ALTER TABLE public.mail_template_categories ADD CONSTRAINT mail_template_categories_pk PRIMARY KEY (id);
ALTER TABLE public.mail_template_types ADD CONSTRAINT mail_template_types_pk PRIMARY KEY (id);
ALTER TABLE public.mail_templates ADD CONSTRAINT mail_templates_pk PRIMARY KEY (id);
ALTER TABLE public.oauth_accounts ADD CONSTRAINT oauth_accounts_pk PRIMARY KEY (id);
ALTER TABLE public.system_settings ADD CONSTRAINT system_settings_pk PRIMARY KEY (id);
ALTER TABLE public.system_settings ADD CONSTRAINT system_settings_pk_2 UNIQUE (config_key);
ALTER TABLE public.tenant_department_member_relations ADD CONSTRAINT tenant_department_member_relations_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_departments ADD CONSTRAINT tenant_departments_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_invitation_records ADD CONSTRAINT tenant_invitation_records_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_invitations ADD CONSTRAINT tenant_invitations_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_member_role_relations ADD CONSTRAINT tenant_member_role_relations_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_members ADD CONSTRAINT tenant_members_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_permissions ADD CONSTRAINT tenant_permissions_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_role_permission_relations ADD CONSTRAINT tenant_role_permission_relations_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_tire_types ADD CONSTRAINT tenant_tire_types_pk PRIMARY KEY (id);
ALTER TABLE public.tenants ADD CONSTRAINT tenants_pk PRIMARY KEY (id);
ALTER TABLE public.user_permissions ADD CONSTRAINT user_permissions_pk_2 PRIMARY KEY (id);
ALTER TABLE public.user_role_permission_relations ADD CONSTRAINT user_role_permission_relations_pk PRIMARY KEY (id);
ALTER TABLE public.user_role_relations ADD CONSTRAINT user_role_relations_pk PRIMARY KEY (id);
ALTER TABLE public.user_roles ADD CONSTRAINT user_roles_pk PRIMARY KEY (id);
ALTER TABLE public.users ADD CONSTRAINT users_pk UNIQUE (username);
ALTER TABLE public.users ADD CONSTRAINT users_pk_2 PRIMARY KEY (id);

-- Indexes
CREATE INDEX tenants_name_index ON public.tenants USING btree (name);
CREATE INDEX tenants_status_index ON public.tenants USING btree (status);
CREATE INDEX tenants_tire_type_id_index ON public.tenants USING btree (tire_type_id);
