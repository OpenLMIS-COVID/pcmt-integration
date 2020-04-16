CREATE TABLE configurations (
  id UUID NOT NULL,
  name TEXT NOT NULL,
  targetUrl TEXT NOT NULL,
  CONSTRAINT configuration_pkey PRIMARY KEY (id)
);

CREATE TABLE configuration_authentication_details (
  id UUID NOT NULL,
  type TEXT NOT NULL,
  username TEXT,
  password TEXT,
  token TEXT,
  CONSTRAINT configuration_authentication_pkey PRIMARY KEY (id),
  CONSTRAINT configuration_authentication_fkey FOREIGN KEY (id) REFERENCES configurations(id)
);

CREATE TABLE integrations (
  id UUID NOT NULL,
  name TEXT NOT NULL,
  programId UUID NOT NULL,
  cronExpression TEXT NOT NULL,
  configurationId UUID NOT NULL,
  CONSTRAINT integration_pkey PRIMARY KEY (id),
  CONSTRAINT integration_fkey FOREIGN KEY (configurationId) REFERENCES configurations(id)
);

CREATE TABLE executions (
  id UUID NOT NULL,
  manualExecution BOOLEAN NOT NULL,
  programId UUID NOT NULL,
  facilityId UUID,
  processingPeriodId UUID NOT NULL,
  targetUrl TEXT NOT NULL,
  startDate TIMESTAMP WITH TIME ZONE NOT NULL,
  endDate TIMESTAMP WITH TIME ZONE,
  CONSTRAINT execution_pkey PRIMARY KEY (id)
);

CREATE TABLE execution_responses (
  id UUID NOT NULL,
  responseDate TIMESTAMP WITH TIME ZONE NOT NULL,
  statusCode INT NOT NULL,
  body TEXT NOT NULL,
  CONSTRAINT execution_response_pkey PRIMARY KEY (id),
  CONSTRAINT execution_response_fkey FOREIGN KEY (id) REFERENCES executions(id)
);

CREATE UNIQUE INDEX configuration_name_unique ON configurations(LOWER(name));
CREATE UNIQUE INDEX configuration_target_url_unique ON configurations(LOWER(targetUrl));

CREATE UNIQUE INDEX integration_name_unique ON integrations(LOWER(name));
CREATE UNIQUE INDEX integration_program_id_unique ON integrations(programId);
