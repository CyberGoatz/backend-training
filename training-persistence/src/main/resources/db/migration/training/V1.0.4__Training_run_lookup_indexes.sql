CREATE INDEX IF NOT EXISTS training_run_state_sandbox_allocation_index
    ON training_run (state, sandbox_instance_allocation_id)
    WHERE sandbox_instance_allocation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS training_run_user_ref_state_instance_id_index
    ON training_run (user_ref_id, state, training_instance_id, id);
