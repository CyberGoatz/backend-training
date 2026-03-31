package cz.cyberrange.platform.training.api.validation;

import java.util.Comparator;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OrderValidator implements ConstraintValidator<ValidOrder, List<? extends Ordered>> {

  @Override
  public void initialize(ValidOrder constraintAnnotation) {}

  @Override
  public boolean isValid(List<? extends Ordered> entities, ConstraintValidatorContext context) {
    if (entities == null) {
      return true;
    }
    int actualOrder = 0;
    entities.sort(Comparator.comparingInt(Ordered::getOrder));
    for (Ordered entity : entities) {
      if (entity.getOrder() != actualOrder) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "The order of the entity '"
                    + entity.getClass()
                    + "' has unexpected order value."
                    + " Expected order is "
                    + actualOrder
                    + ", but actual is "
                    + entity.getOrder()
                    + ".")
            .addConstraintViolation();
        return false;
      }
      actualOrder++;
    }

    return true;
  }
}
