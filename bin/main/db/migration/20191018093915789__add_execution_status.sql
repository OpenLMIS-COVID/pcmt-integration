-- a new column is added ...
ALTER TABLE executions ADD COLUMN status VARCHAR(255);

-- ... for all executions that have informational responses (100 - 199),
-- successful responses (200 - 299), or redirect responses (300 - 399)
-- the SUCCESS status is set ...
UPDATE executions
  SET status = 'SUCCESS'
  WHERE id IN (SELECT id FROM execution_responses WHERE statusCode BETWEEN 100 AND 399);

-- ... and for other executions the ERROR status is set ...
UPDATE executions
 SET status = 'ERROR'
 WHERE status IS NULL;

--- ... in the end the NULL value is not allowed.
ALTER TABLE executions ALTER COLUMN status SET NOT NULL;
