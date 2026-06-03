CREATE TABLE training_level_result (
    id                      bigserial NOT NULL,
    training_run_id         int8      NOT NULL,
    level_id                int8      NOT NULL,
    participant_level_score int4      NOT NULL DEFAULT 0,
    wrong_answers           int4      NOT NULL DEFAULT 0,
    hints_taken             int4      NOT NULL DEFAULT 0,
    solution_taken          boolean   NOT NULL DEFAULT FALSE,
    completed               boolean   NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uq_training_level_result_run_level UNIQUE (training_run_id, level_id),
    FOREIGN KEY (training_run_id) REFERENCES training_run,
    FOREIGN KEY (level_id) REFERENCES abstract_level
);

CREATE INDEX idx_training_level_result_run_id ON training_level_result(training_run_id);
