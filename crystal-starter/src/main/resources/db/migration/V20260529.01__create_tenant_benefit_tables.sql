-- tenant_tire_benefit_features
-- Feature catalog: defines what benefit/entitlement items exist in the system
CREATE TABLE IF NOT EXISTS public.tenant_tire_benefit_features (
    id bigint NOT NULL,
    feature_key character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    description character varying(512),
    feature_type integer NOT NULL,
    default_value character varying(255),
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

-- tenant_tire_benefit_values
-- Tier → Feature value mapping: what value each tier gets for each feature
CREATE TABLE IF NOT EXISTS public.tenant_tire_benefit_values (
    id bigint NOT NULL,
    tire_type_id bigint NOT NULL,
    feature_id bigint NOT NULL,
    feature_value character varying(255) NOT NULL,
    created_time bigint NOT NULL,
    modified_time bigint NOT NULL,
    deleted_time bigint
);

CREATE INDEX IF NOT EXISTS tenant_tire_benefit_values_tire_type_id_index
    ON public.tenant_tire_benefit_values USING btree (tire_type_id);

CREATE INDEX IF NOT EXISTS tenant_tire_benefit_values_feature_id_index
    ON public.tenant_tire_benefit_values USING btree (feature_id);

CREATE UNIQUE INDEX IF NOT EXISTS tenant_tire_benefit_values_unique_index
    ON public.tenant_tire_benefit_values USING btree (tire_type_id, feature_id);
