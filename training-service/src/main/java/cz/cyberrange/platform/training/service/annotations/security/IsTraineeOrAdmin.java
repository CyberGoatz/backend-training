package cz.cyberrange.platform.training.service.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * The custom annotation <i>@IsTraineeOrAdmin<i/>. All methods annotated with this annotation expect
 * the user has a role <strong>ROLE_TRAINING_ADMINISTRATOR<strong/> or
 * <strong>ROLE_TRAINING_TRAINEE<strong/>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "hasAnyAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE, "
        + "T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
public @interface IsTraineeOrAdmin {}
