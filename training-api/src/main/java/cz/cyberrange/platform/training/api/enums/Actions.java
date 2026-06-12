package cz.cyberrange.platform.training.api.enums;

/**
 * The enumeration of Actions.
 *
 */
public enum Actions {

    /**
     * None actions.
     */
    NONE,
    /**
     * Shows results of finished training runs.
     */
    RESULTS,

    /**
     * Resume actions.
     */
    RESUME,

    /**
     * Sandbox assigned to the training run expired and the run cannot be resumed.
     */
    SANDBOX_EXPIRED;
}
