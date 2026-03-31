package cz.cyberrange.platform.training.service.startup;

import javax.validation.constraints.NotEmpty;

public class DefaultInfoLevel {
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  private String title;

  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
