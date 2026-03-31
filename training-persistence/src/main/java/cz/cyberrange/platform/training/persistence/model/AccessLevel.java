package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Class specifying Abstract level as access level. Access levels contain instructions on how to
 * connect to the virtual machines.
 */
@Entity
@Table(name = "access_level")
@PrimaryKeyJoinColumn(name = "id")
public class AccessLevel extends AbstractLevel {

  @Column(name = "passkey")
  private String passkey;

  @Lob
  @Column(name = "cloud_content", nullable = false)
  private String cloudContent;

  @Lob
  @Column(name = "local_content", nullable = false)
  private String localContent;

  /**
   * Gets passkey that needs to be entered by trainee to complete level
   *
   * @return the passkey
   */
  public String getPasskey() {
    return passkey;
  }

  /**
   * Sets passkey that needs to be entered by trainee to complete level
   *
   * @param passkey the passkey
   */
  public void setPasskey(String passkey) {
    this.passkey = passkey;
  }

  /**
   * Gets instructions on how to access machine in cloud environment
   *
   * @return the cloud content
   */
  public String getCloudContent() {
    return cloudContent;
  }

  /**
   * Sets instructions on how to access machine in cloud environment
   *
   * @param cloudContent the cloud content
   */
  public void setCloudContent(String cloudContent) {
    this.cloudContent = cloudContent;
  }

  /**
   * Gets instructions on how to access machine in local (non-cloud) environment
   *
   * @return the local content
   */
  public String getLocalContent() {
    return localContent;
  }

  /**
   * Sets instructions on how to access machine in local (non-cloud) environment
   *
   * @param localContent the local content
   */
  public void setLocalContent(String localContent) {
    this.localContent = localContent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccessLevel)) return false;
    if (!super.equals(o)) return false;
    AccessLevel trainingLevel = (AccessLevel) o;
    return Objects.equals(getCloudContent(), trainingLevel.getCloudContent())
        && Objects.equals(getLocalContent(), trainingLevel.getLocalContent());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getCloudContent(), getLocalContent());
  }

  @Override
  public String toString() {
    return "AccessLevel{"
        + "passkey='"
        + passkey
        + '\''
        + ", cloudContent='"
        + cloudContent
        + '\''
        + ", localContent='"
        + localContent
        + '\''
        + '}';
  }
}
