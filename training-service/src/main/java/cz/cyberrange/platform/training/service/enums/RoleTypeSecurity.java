package cz.cyberrange.platform.training.service.enums;

/** The enumeration of Role types used for security. */
public enum RoleTypeSecurity {
  /** Role of training administrator. */
  ROLE_TRAINING_ADMINISTRATOR,
  /** Role of training designer permits user to work with training definitions. */
  ROLE_TRAINING_DESIGNER,
  /** Role of training organizer permits user to work with training instances. */
  ROLE_TRAINING_ORGANIZER,
  /** Role of training trainee permits user to work with training runs. */
  ROLE_TRAINING_TRAINEE
}
