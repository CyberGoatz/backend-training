package cz.cyberrange.platform.training.service.startup;

import javax.validation.Valid;

public class DefaultLevels {
  @Valid private DefaultInfoLevel defaultInfoLevel;
  @Valid private DefaultAccessLevel defaultAccessLevel;

  public DefaultInfoLevel getDefaultInfoLevel() {
    return defaultInfoLevel;
  }

  public void setDefaultInfoLevel(DefaultInfoLevel defaultInfoLevel) {
    this.defaultInfoLevel = defaultInfoLevel;
  }

  public DefaultAccessLevel getDefaultAccessLevel() {
    return defaultAccessLevel;
  }

  public void setDefaultAccessLevel(DefaultAccessLevel defaultAccessLevel) {
    this.defaultAccessLevel = defaultAccessLevel;
  }
}
