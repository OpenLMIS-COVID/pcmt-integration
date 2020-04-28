CREATE TABLE integrations (
  id UUID NOT NULL,
  name TEXT NOT NULL,
  cronExpression TEXT NOT NULL,
  CONSTRAINT integration_pkey PRIMARY KEY (id)
);

CREATE TABLE executions (
  id UUID NOT NULL,
  manualExecution BOOLEAN NOT NULL,
  facilityId UUID,
  processingPeriodId UUID NOT NULL,
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

CREATE UNIQUE INDEX integration_name_unique ON integrations(LOWER(name));
