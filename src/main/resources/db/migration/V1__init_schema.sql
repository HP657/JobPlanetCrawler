-- CREATE UNIQUE INDEX IF NOT EXISTS idx_job_link_title
--     ON job_openings (link, title);

-- DROP INDEX idx_job_link_title;

CREATE UNIQUE INDEX idx_job_link
    ON job_openings(link);