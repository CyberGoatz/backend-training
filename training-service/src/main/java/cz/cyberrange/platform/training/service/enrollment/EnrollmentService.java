package cz.cyberrange.platform.training.service.enrollment;

import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import org.springframework.stereotype.Service;

/**
 * Checks that a trainee may start a run of a training instance restricted to a user-and-group group.
 * Membership is asked from user-and-group on every check and never cached, so removing a student from the group
 * takes effect immediately.
 */
@Service
public class EnrollmentService {

    private final GroupMembershipClient groupMembershipClient;

    public EnrollmentService(GroupMembershipClient groupMembershipClient) {
        this.groupMembershipClient = groupMembershipClient;
    }

    /**
     * @param trainingInstance the instance the trainee is accessing.
     * @param userRefId        the trainee's user-and-group id.
     * @throws NotEnrolledException if the instance is restricted to a group and the trainee is not a member.
     */
    public void checkEnrolled(TrainingInstance trainingInstance, Long userRefId) {
        Long allowedGroupId = trainingInstance.getAllowedGroupId();
        if (allowedGroupId == null) {
            return;
        }
        if (userRefId == null || !groupMembershipClient.isMember(allowedGroupId, userRefId)) {
            throw new NotEnrolledException();
        }
    }
}
