package cz.cyberrange.platform.training.service.utils;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import java.util.Set;

public enum CheatingDetectionUtils {
  ;

  public static boolean checkIfContainsParticipant(
      Set<DetectionEventParticipant> participants, DetectionEventParticipant participant) {
    return participants.stream().anyMatch(elem -> elem.getUserId().equals(participant.getUserId()));
  }

  public static DetectionEventParticipant extractParticipant(Submission s, String participantName) {
    return extractParticipant(s, false, 0L, participantName);
  }

  public static DetectionEventParticipant extractParticipant(
      Submission s, boolean isMinimal, Long solvedInTime, String participantName) {
    DetectionEventParticipant participant = new DetectionEventParticipant();
    participant.setIpAddress(s.getIpAddress());
    participant.setUserId(s.getTrainingRun().getParticipantRef().getUserRefId());
    participant.setOccurredAt(s.getDate());
    participant.setParticipantName(participantName);
    if (isMinimal) {
      participant.setSolvedInTime(solvedInTime);
    }
    return participant;
  }

  public static String generateParticipantString(Set<DetectionEventParticipant> participants) {
    StringBuilder participantString = new StringBuilder();
    for (var participant : participants) {
      participantString.append(',').append(' ').append(participant.getParticipantName());
    }
    participantString.delete(0, 2);
    return participantString.toString();
  }
}
