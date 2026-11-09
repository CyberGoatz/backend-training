-- Campus tenancy (additive). A NULL tenant_key marks a row of the global
-- library or a row created by a caller without an organization claim, so
-- existing data and the public stack keep their current behavior.
ALTER TABLE training_definition ADD COLUMN IF NOT EXISTS tenant_key varchar(40) NULL;
ALTER TABLE training_instance ADD COLUMN IF NOT EXISTS tenant_key varchar(40) NULL;
-- UAG group whose members may start runs of the instance (NULL = anyone with the token).
ALTER TABLE training_instance ADD COLUMN IF NOT EXISTS allowed_group_id bigint NULL;

CREATE INDEX IF NOT EXISTS training_definition_tenant_key_index ON training_definition (tenant_key);
CREATE INDEX IF NOT EXISTS training_instance_tenant_key_index ON training_instance (tenant_key);
