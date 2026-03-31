package cz.cyberrange.platform.training.api.validation;

/** The annotated element must not be {@code null}. Accepts any type. */
public interface Ordered {

  int getOrder();
}
