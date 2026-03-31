package cz.cyberrange.platform.training.api.responses;

import io.swagger.annotations.ApiModel;

@ApiModel(
    value = "SandboxDefinitionInfo",
    description = "Basic information about the sandbox definition.")
public class SandboxDefinitionInfo {
  private Long id;
  private String name;
  private String url;
  private String rev;

  public SandboxDefinitionInfo() {}

  public SandboxDefinitionInfo(Long id, String name, String url, String rev) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.rev = rev;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRev() {
    return rev;
  }

  public void setRev(String rev) {
    this.rev = rev;
  }

  @Override
  public String toString() {
    return "SandboxDefinitionInfo{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", url='"
        + url
        + '\''
        + ", rev='"
        + rev
        + '\''
        + '}';
  }
}
