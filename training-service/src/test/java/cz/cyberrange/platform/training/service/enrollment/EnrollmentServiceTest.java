package cz.cyberrange.platform.training.service.enrollment;

import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnrollmentServiceTest {

    private final GroupMembershipClient membershipClient = mock(GroupMembershipClient.class);
    private final EnrollmentService enrollmentService = new EnrollmentService(membershipClient);

    @Test
    void openInstancesNeedNoMembership() {
        TrainingInstance instance = new TrainingInstance();
        assertDoesNotThrow(() -> enrollmentService.checkEnrolled(instance, 7L));
        verify(membershipClient, never()).isMember(anyLong(), anyLong());
    }

    @Test
    void membersOfTheAllowedGroupMayStart() {
        when(membershipClient.isMember(12L, 7L)).thenReturn(true);
        assertDoesNotThrow(() -> enrollmentService.checkEnrolled(restrictedTo(12L), 7L));
    }

    @Test
    void nonMembersAreRejectedWithNotEnrolled() {
        when(membershipClient.isMember(12L, 7L)).thenReturn(false);
        NotEnrolledException ex = assertThrows(NotEnrolledException.class, () -> enrollmentService.checkEnrolled(restrictedTo(12L), 7L));
        assertEquals("NOT_ENROLLED", ex.getCode());
        assertEquals(ForbiddenException.class, ex.getClass().getSuperclass(), "answered as 403");
    }

    @Test
    void membershipIsAskedOnEveryAccess() {
        when(membershipClient.isMember(12L, 7L)).thenReturn(true, false);
        TrainingInstance instance = restrictedTo(12L);
        enrollmentService.checkEnrolled(instance, 7L);
        assertThrows(NotEnrolledException.class, () -> enrollmentService.checkEnrolled(instance, 7L));
        verify(membershipClient, times(2)).isMember(12L, 7L);
    }

    @Test
    void unknownCallerIsRejected() {
        assertThrows(NotEnrolledException.class, () -> enrollmentService.checkEnrolled(restrictedTo(12L), null));
    }

    private static TrainingInstance restrictedTo(long groupId) {
        TrainingInstance instance = new TrainingInstance();
        instance.setAllowedGroupId(groupId);
        return instance;
    }
}
