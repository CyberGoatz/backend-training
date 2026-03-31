package cz.cyberrange.platform.training.persistence.model;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReferenceSolutionNode {

  private String stateName;
  private List<String> prereqState;
  private String cmd;
  private String cmdType;
  private String cmdRegex;
  private boolean optional;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReferenceSolutionNode)) return false;
    ReferenceSolutionNode that = (ReferenceSolutionNode) o;
    return isOptional() == that.isOptional()
        && Objects.equals(getStateName(), that.getStateName())
        && Objects.equals(getPrereqState(), that.getPrereqState())
        && Objects.equals(getCmd(), that.getCmd())
        && Objects.equals(getCmdType(), that.getCmdType())
        && Objects.equals(getCmdRegex(), that.getCmdRegex());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getStateName(), getPrereqState(), getCmd(), getCmdType(), getCmdRegex(), isOptional());
  }

  @Override
  public String toString() {
    return "ReferenceSolutionNode{"
        + "stateName='"
        + stateName
        + '\''
        + ", prereqState="
        + prereqState
        + ", cmd='"
        + cmd
        + '\''
        + ", cmdType='"
        + cmdType
        + '\''
        + ", cmdRegex='"
        + cmdRegex
        + '\''
        + ", optional="
        + optional
        + '}';
  }
}
