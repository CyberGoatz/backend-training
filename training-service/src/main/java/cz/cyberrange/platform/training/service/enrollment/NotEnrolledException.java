package cz.cyberrange.platform.training.service.enrollment;

import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;

/**
 * The caller is not a member of the group a training instance is restricted to. Answered with 403 and the error code
 * {@value #CODE}.
 */
public class NotEnrolledException extends ForbiddenException {

    public static final String CODE = "NOT_ENROLLED";

    public NotEnrolledException() {
        super("You are not enrolled in the course this training belongs to.");
    }

    public String getCode() {
        return CODE;
    }
}
