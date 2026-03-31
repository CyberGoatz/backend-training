package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class LockedPoolInfo {

  private long id;

  @JsonProperty(value = "pool_id")
  private long poolId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getPoolId() {
    return poolId;
  }

  public void setPoolId(long poolId) {
    this.poolId = poolId;
  }

  @Override
  public String toString() {
    return "LockedPoolInfo{" + "id=" + id + ", poolId=" + poolId + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LockedPoolInfo)) return false;
    LockedPoolInfo that = (LockedPoolInfo) o;
    return getId() == that.getId() && getPoolId() == that.getPoolId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getPoolId());
  }
}
