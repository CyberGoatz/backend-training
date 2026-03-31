package cz.cyberrange.platform.training.persistence.model.detection;

import java.util.List;

public class ParticipantGroups {
  private List<List<Long>> userIdGroups;
  private List<List<Long>> eventIdGroups;

  public ParticipantGroups(List<List<Long>> userIdGroups, List<List<Long>> eventIdGroups) {
    this.userIdGroups = userIdGroups;
    this.eventIdGroups = eventIdGroups;
  }

  public List<List<Long>> getUserIdGroups() {
    return userIdGroups;
  }

  public List<List<Long>> getEventIdGroups() {
    return eventIdGroups;
  }
}
