package cz.cyberrange.platform.training.persistence.model.detection;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@ToString
@Entity
@Table(name = "no_commands_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "NoCommandsDetectionEvent.findNoCommandsEventById",
                query = "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.id = :eventId"
        ),
        @NamedQuery(
                name = "NoCommandsDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT ncde FROM NoCommandsDetectionEvent ncde WHERE ncde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class NoCommandsDetectionEvent extends AbstractDetectionEvent { }
