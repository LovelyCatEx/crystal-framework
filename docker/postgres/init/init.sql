--
-- PostgreSQL database dump
--

-- Dumped from database version 15.16 (Debian 15.16-1.pgdg13+1)
-- Dumped by pg_dump version 15.16 (Debian 15.16-1.pgdg13+1)

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'db') THEN
            CREATE DATABASE db;
        END IF;
    END
$$;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: file_resources; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.file_resources (
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


ALTER TABLE public.file_resources OWNER TO postgres;

--
-- Name: mail_template_categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mail_template_categories (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);


ALTER TABLE public.mail_template_categories OWNER TO postgres;

--
-- Name: mail_template_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mail_template_types (
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


ALTER TABLE public.mail_template_types OWNER TO postgres;

--
-- Name: mail_templates; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mail_templates (
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


ALTER TABLE public.mail_templates OWNER TO postgres;

--
-- Name: oauth_accounts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.oauth_accounts (
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


ALTER TABLE public.oauth_accounts OWNER TO postgres;

--
-- Name: storage_providers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.storage_providers (
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


ALTER TABLE public.storage_providers OWNER TO postgres;

--
-- Name: system_settings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.system_settings (
    id bigint NOT NULL,
    config_key character varying(256) NOT NULL,
    config_value text,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);


ALTER TABLE public.system_settings OWNER TO postgres;

--
-- Name: user_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_permissions (
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


ALTER TABLE public.user_permissions OWNER TO postgres;

--
-- Name: user_role_permission_relations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_role_permission_relations (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);


ALTER TABLE public.user_role_permission_relations OWNER TO postgres;

--
-- Name: user_role_relations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_role_relations (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);


ALTER TABLE public.user_role_relations OWNER TO postgres;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
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


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: mail_template_categories mail_template_categories_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mail_template_categories
    ADD CONSTRAINT mail_template_categories_pk PRIMARY KEY (id);


--
-- Name: mail_template_types mail_template_types_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mail_template_types
    ADD CONSTRAINT mail_template_types_pk PRIMARY KEY (id);


--
-- Name: mail_templates mail_templates_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mail_templates
    ADD CONSTRAINT mail_templates_pk PRIMARY KEY (id);


--
-- Name: oauth_accounts oauth_accounts_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.oauth_accounts
    ADD CONSTRAINT oauth_accounts_pk PRIMARY KEY (id);


--
-- Name: system_settings system_settings_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_pk PRIMARY KEY (id);


--
-- Name: system_settings system_settings_pk_2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_pk_2 UNIQUE (config_key);


--
-- Name: user_permissions user_permissions_pk_2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_permissions
    ADD CONSTRAINT user_permissions_pk_2 PRIMARY KEY (id);


--
-- Name: user_role_permission_relations user_role_permission_relations_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_permission_relations
    ADD CONSTRAINT user_role_permission_relations_pk PRIMARY KEY (id);


--
-- Name: user_role_relations user_role_relations_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_relations
    ADD CONSTRAINT user_role_relations_pk PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pk PRIMARY KEY (id);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk UNIQUE (username);


--
-- Name: users users_pk_2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk_2 PRIMARY KEY (id);