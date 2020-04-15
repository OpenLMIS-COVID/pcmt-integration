ALTER TABLE executions ADD COLUMN requestBody TEXT;

UPDATE executions SET requestBody = '{}';

ALTER TABLE executions ALTER COLUMN requestBody SET NOT NULL;
