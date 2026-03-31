package cz.cyberrange.platform.training.persistence.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Group of users that can test Training runs created from unreleased Training Definition */
@Entity
@Getter
@Setter
@Table(name = "beta_testing_group")
public class BetaTestingGroup extends AbstractEntity<Long> {

  /** Set of users allowed to test associated Training Definition */
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinTable(
      name = "beta_testing_group_user_ref",
      joinColumns = @JoinColumn(name = "beta_testing_group_id"),
      inverseJoinColumns = @JoinColumn(name = "user_ref_id"))
  private Set<UserRef> organizers = new HashSet<>();

  /** Associated Training Definition */
  @OneToOne(mappedBy = "betaTestingGroup", fetch = FetchType.LAZY)
  private TrainingDefinition trainingDefinition;

  /**
   * Gets set of users allowed to test associated Training Definition
   *
   * @return the organizers
   */
  public Set<UserRef> getOrganizers() {
    return Collections.unmodifiableSet(organizers);
  }

  /**
   * Adds organizer to set of users allowed to test associated Training Definition
   *
   * @param organizer to be added
   */
  public void addOrganizer(UserRef organizer) {
    this.organizers.add(organizer);
    organizer.addViewGroup(this);
  }

  /**
   * Removes organizer from set of users allowed to test associated Training Definition
   *
   * @param organizer to be removed
   */
  public void removeOrganizer(UserRef organizer) {
    this.organizers.remove(organizer);
    organizer.removeViewGroup(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BetaTestingGroup)) return false;
    BetaTestingGroup that = (BetaTestingGroup) o;
    return Objects.equals(getOrganizers(), that.getOrganizers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOrganizers());
  }

  @Override
  public String toString() {
    return "BetaTestingGroup{" + "id=" + super.getId() + '}';
  }
}
