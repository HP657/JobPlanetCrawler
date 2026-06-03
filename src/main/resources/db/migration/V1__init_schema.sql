CREATE UNIQUE INDEX IF NOT EXISTS idx_job_link_title
    ON job_openings (link, title);