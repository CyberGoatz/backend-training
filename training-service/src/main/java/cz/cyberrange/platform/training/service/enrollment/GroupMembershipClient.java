package cz.cyberrange.platform.training.service.enrollment;

/**
 * Answers whether a user is a member of a user-and-group group.
 */
public interface GroupMembershipClient {

    /**
     * @param groupId   id of the group in user-and-group.
     * @param userRefId id of the user in user-and-group.
     * @return true if the user is currently a member of the group.
     */
    boolean isMember(long groupId, long userRefId);
}
