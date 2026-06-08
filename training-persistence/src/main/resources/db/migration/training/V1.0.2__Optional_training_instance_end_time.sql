ALTER TABLE training_instance
    ALTER COLUMN end_time DROP NOT NULL;

ALTER TABLE training_run
    ALTER COLUMN end_time DROP NOT NULL;
