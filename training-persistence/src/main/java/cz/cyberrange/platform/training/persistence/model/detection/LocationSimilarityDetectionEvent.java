package cz.cyberrange.platform.training.persistence.model.detection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "location_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "LocationSimilarityDetectionEvent.findLocationSimilarityEventById",
      query = "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.id = :eventId"),
  @NamedQuery(
      name = "LocationSimilarityDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.cheatingDetectionId = :cheatingDetectionId")
})
public class LocationSimilarityDetectionEvent extends AbstractDetectionEvent {

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "dns")
  private String dns;

  @Column(name = "is_address_deploy")
  private boolean isAddressDeploy;
}
