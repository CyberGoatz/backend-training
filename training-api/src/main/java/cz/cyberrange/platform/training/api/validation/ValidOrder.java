package cz.cyberrange.platform.training.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated element must be of type {@link java.util.List} and have correct order of elements
 * (0,1,2,...). For correct behaviour each element of list must implements {@link Ordered}
 * interface.
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = OrderValidator.class)
@Documented
public @interface ValidOrder {
  String message() default "Invalid order of questions.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
