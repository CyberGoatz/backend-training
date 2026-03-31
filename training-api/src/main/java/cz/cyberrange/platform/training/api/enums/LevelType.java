package cz.cyberrange.platform.training.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/** The enumeration of Level types. */
public enum LevelType {

  /** Info level type. */
  INFO_LEVEL,
  /** Training level type. */
  TRAINING_LEVEL,
  /** Access level type. */
  ACCESS_LEVEL,
  /** Assessment level type. */
  ASSESSMENT_LEVEL;

  @JsonCreator
  public static LevelType createLevelType(String levelType) {
    if (levelType.equals("GAME_LEVEL")) {
      levelType = "TRAINING_LEVEL";
    }
    for (LevelType type : LevelType.values()) {
      if (type.name().equals(levelType)) {
        return type;
      }
    }
    return null;
  }

  public String getName() {
    return this.name();
  }
}
