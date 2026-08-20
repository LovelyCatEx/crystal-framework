-- Station-message (in-app messaging) tables: conversations, parties, members,
-- messages (write-fanout side) plus broadcasts and read markers (read-fanout side).

CREATE TABLE IF NOT EXISTS public.msg_conversations (
    id bigint NOT NULL,
    scope_type integer NOT NULL,
    scope_id bigint,
    conversation_kind integer NOT NULL,
    dedupe_key character varying(512) NOT NULL,
    last_message_id bigint,
    last_message_time bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_conversations ADD CONSTRAINT msg_conversations_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX msg_conversations_dedupe_key_uidx
    ON public.msg_conversations USING btree (dedupe_key) WHERE deleted_time IS NULL;
CREATE INDEX msg_conversations_scope_index
    ON public.msg_conversations USING btree (scope_type, scope_id, last_message_time);

CREATE TABLE IF NOT EXISTS public.msg_conversation_parties (
    id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    party_type integer NOT NULL,
    party_id bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_conversation_parties ADD CONSTRAINT msg_conversation_parties_pk PRIMARY KEY (id);
CREATE INDEX msg_conversation_parties_conversation_index
    ON public.msg_conversation_parties USING btree (conversation_id, deleted_time);

CREATE TABLE IF NOT EXISTS public.msg_conversation_members (
    id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    user_id bigint NOT NULL,
    last_read_message_id bigint,
    unread_count integer NOT NULL DEFAULT 0,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_conversation_members ADD CONSTRAINT msg_conversation_members_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX msg_conversation_members_conv_user_uidx
    ON public.msg_conversation_members USING btree (conversation_id, user_id) WHERE deleted_time IS NULL;
CREATE INDEX msg_conversation_members_user_index
    ON public.msg_conversation_members USING btree (user_id, deleted_time);

CREATE TABLE IF NOT EXISTS public.msg_messages (
    id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender_party_type integer NOT NULL,
    sender_party_id bigint,
    acting_user_id bigint,
    content_type integer NOT NULL,
    content text NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_messages ADD CONSTRAINT msg_messages_pk PRIMARY KEY (id);
CREATE INDEX msg_messages_conversation_index
    ON public.msg_messages USING btree (conversation_id, id, deleted_time);

CREATE TABLE IF NOT EXISTS public.msg_broadcasts (
    id bigint NOT NULL,
    scope_type integer NOT NULL,
    scope_id bigint,
    sender_party_type integer NOT NULL,
    sender_party_id bigint,
    acting_user_id bigint,
    category integer NOT NULL DEFAULT 0,
    audience_type integer NOT NULL,
    audience_ref bigint,
    title character varying(256) NOT NULL,
    content text NOT NULL,
    publish_time bigint NOT NULL,
    expire_time bigint,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_broadcasts ADD CONSTRAINT msg_broadcasts_pk PRIMARY KEY (id);
CREATE INDEX msg_broadcasts_audience_index
    ON public.msg_broadcasts USING btree (category, audience_type, audience_ref, publish_time, deleted_time);

CREATE TABLE IF NOT EXISTS public.msg_broadcast_reads (
    id bigint NOT NULL,
    broadcast_id bigint NOT NULL,
    user_id bigint NOT NULL,
    read_time bigint NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);
ALTER TABLE public.msg_broadcast_reads ADD CONSTRAINT msg_broadcast_reads_pk PRIMARY KEY (id);
CREATE UNIQUE INDEX msg_broadcast_reads_bc_user_uidx
    ON public.msg_broadcast_reads USING btree (broadcast_id, user_id) WHERE deleted_time IS NULL;
CREATE INDEX msg_broadcast_reads_user_index
    ON public.msg_broadcast_reads USING btree (user_id, deleted_time);
