package cz.cyberrange.platform.training.service.annotations.transactions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Extending of the class {@link Transactional} which has <i>read-only</i> set to true. */
@Transactional(rollbackFor = Exception.class, readOnly = true)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalRO {

  /**
   * Value string.
   *
   * @return the string
   */
  @AliasFor("transactionManager")
  String value() default "";

  /**
   * Transaction manager string.
   *
   * @return the string
   */
  @AliasFor("value")
  String transactionManager() default "";

  /**
   * Propagation propagation.
   *
   * @return the propagation
   */
  Propagation propagation() default Propagation.REQUIRED;

  /**
   * Isolation isolation.
   *
   * @return the isolation
   */
  Isolation isolation() default Isolation.DEFAULT;

  /**
   * Timeout int.
   *
   * @return the int
   */
  int timeout() default -1;
}
