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
@Table(name = "minimal_solve_time_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "MinimalSolveTimeDetectionEvent.findMinimalSolveTimeEventById",
                query = "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.id = :eventId"
        ),
        @NamedQuery(
                name = "MinimalSolveTimeDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT mstde FROM MinimalSolveTimeDetectionEvent mstde WHERE mstde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class MinimalSolveTimeDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "minimal_solve_time")
    private Long minimalSolveTime;
}
