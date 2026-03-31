package cz.cyberrange.platform.training.service.mapping.modelmapper;

import cz.cyberrange.platform.training.api.responses.PageResultResource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;

/**
 * That class is used for mapping entities to DTO classes. Example of usage for this mapper shown
 * below:
 *
 * <pre>
 * <code>
 * public class UserFacadeImpl implements UserFacade {
 *
 * &#64;Autowired
 * private BeanMapping beanMapping;
 *
 * &#64;Override
 * public List&lt;UserDTO&gt; getAllUsers() {
 * return beanMapping.mapTo(userService.findAllTrainingDefinitions(), UserDTO.class);
 * }
 * }
 * </code>
 * </pre>
 *
 * <p>This example shows mapping list of user entities to list of user DTO classes.
 */
public interface BeanMapping {

  <T> List<T> mapTo(Collection<?> objects, Class<T> mapToClass);

  <T> Page<T> mapTo(Page<?> objects, Class<T> mapToClass);

  <T> PageResultResource<T> mapToPageResultDTO(Page<?> objects, Class<T> mapToClass);

  <T> Set<T> mapToSet(Collection<?> objects, Class<T> mapToClass);

  <T> Optional<T> mapToOptional(Object u, Class<T> mapToClass);

  <T> T mapTo(Object u, Class<T> mapToClass);
}
