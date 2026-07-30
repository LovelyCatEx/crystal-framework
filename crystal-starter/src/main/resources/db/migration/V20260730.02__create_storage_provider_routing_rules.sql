-- Rule-based storage provider routing: ordered rules matched top-down against an upload's
-- RoutingContext (file type/extension/content-type/size/user/time). condition_tree stores a
-- GroupNode JSON tree (same shape as FilterBuilder's advanced filter); NULL means match-all,
-- used as a fallback rule.
CREATE TABLE IF NOT EXISTS public.storage_provider_routing_rules (
    id bigint NOT NULL,
    name character varying(64) NOT NULL,
    priority integer NOT NULL,
    condition_tree text,
    target_provider_ids text NOT NULL,
    distribution_type integer NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

ALTER TABLE public.storage_provider_routing_rules ADD CONSTRAINT storage_provider_routing_rules_pk PRIMARY KEY (id);

CREATE INDEX storage_provider_routing_rules_priority_index ON public.storage_provider_routing_rules USING btree (priority);
