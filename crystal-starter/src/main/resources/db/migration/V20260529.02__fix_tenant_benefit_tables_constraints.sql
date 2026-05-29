-- Add missing PRIMARY KEY constraints
ALTER TABLE public.tenant_tire_benefit_features ADD CONSTRAINT tenant_tire_benefit_features_pk PRIMARY KEY (id);
ALTER TABLE public.tenant_tire_benefit_values ADD CONSTRAINT tenant_tire_benefit_values_pk PRIMARY KEY (id);

-- Add missing UNIQUE constraint on feature_key
ALTER TABLE public.tenant_tire_benefit_features ADD CONSTRAINT tenant_tire_benefit_features_unique UNIQUE (feature_key);
