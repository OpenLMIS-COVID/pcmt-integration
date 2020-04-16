ALTER TABLE executions ALTER COLUMN programId DROP NOT NULL;

ALTER TABLE integrations ALTER COLUMN programId DROP NOT NULL;
DROP INDEX integration_program_id_unique;

CREATE UNIQUE INDEX integration_programid_configurationid_unq
  ON integrations(programId, configurationId)
  WHERE programId IS NOT NULL;

CREATE UNIQUE INDEX integration_configurationid_unq
  ON integrations(configurationId)
  WHERE programId IS NULL;
