ALTER TABLE job_openings ADD COLUMN updated_at TIMESTAMP(6);
UPDATE job_openings SET updated_at = created_at;
ALTER TABLE job_openings ALTER COLUMN updated_at SET NOT NULL;