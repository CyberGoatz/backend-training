package cz.cyberrange.platform.training.persistence.model.detection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "time_proximity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "TimeProximityDetectionEvent.findTimeProximityEventById",
                query = "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.id = :eventId"
        ),
        @NamedQuery(
                name = "TimeProximityDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT tpde FROM TimeProximityDetectionEvent tpde WHERE tpde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class TimeProximityDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "threshold")
    private Long threshold;
}
