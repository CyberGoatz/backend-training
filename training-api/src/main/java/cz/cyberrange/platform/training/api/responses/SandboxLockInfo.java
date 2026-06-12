package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Basic information about sandbox allocation lock.
 */
public class SandboxLockInfo {

    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "sandbox_id")
    private String sandboxId;
    @JsonProperty(value = "created")
    private OffsetDateTime created;
    @JsonProperty(value = "expires_at")
    private OffsetDateTime expiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
