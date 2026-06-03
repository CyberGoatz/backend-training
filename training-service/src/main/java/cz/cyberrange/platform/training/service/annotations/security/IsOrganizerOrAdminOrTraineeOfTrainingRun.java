package cz.cyberrange.platform.training.service.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsOrganizerOrAdminOrTraineeOfTrainingRun<i/>. All methods annotated with this annotation expect the user has a role
 * <strong>ROLE_TRAINING_ADMINISTRATOR<strong/>, <strong>ROLE_TRAINING_ORGANIZER<strong/>, or is trainee of the given training run.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
        "T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER) " +
        "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
public @interface IsOrganizerOrAdminOrTraineeOfTrainingRun {}
