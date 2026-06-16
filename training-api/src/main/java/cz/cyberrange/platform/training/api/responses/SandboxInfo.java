package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Represents basic information about Sandbox.
 */
public class SandboxInfo {

    private String id;
    @JsonProperty(value = "lock_id")
    private Integer lockId;
    @JsonProperty(value = "allocation_unit_id")
    private Integer allocationUnitId;
    @JsonProperty(value = "expires_at")
    private OffsetDateTime expiresAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getLockId() {
        return lockId;
    }

    public void setLockId(Integer lockId) {
        this.lockId = lockId;
    }

    public Integer getAllocationUnitId() {
        return allocationUnitId;
    }

    public void setAllocationUnitId(Integer allocationUnitId) {
        this.allocationUnitId = allocationUnitId;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "SandboxInfo{" +
                "id=" + id +
                ", lockId=" + lockId +
                ", allocationUnitId=" + allocationUnitId +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
