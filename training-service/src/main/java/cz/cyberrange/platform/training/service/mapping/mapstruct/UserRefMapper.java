package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.export.UserRefExportDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * The UserRefMapper is an utility class to map items into data transfer objects. It provides the
 * implementation of mappings between Java bean type UserRefMapper and DTOs classes. Code is
 * generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {TrainingInstanceMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRefMapper extends ParentMapper {

  UserRef mapToEntity(UserRefDTO dto);

  UserRefDTO mapToDTO(UserRef entity);

  List<UserRef> mapToList(Collection<UserRefDTO> dtos);

  List<UserRefDTO> mapToListDTO(Collection<UserRef> entities);

  Set<UserRef> mapToSet(Collection<UserRefDTO> dtos);

  Set<UserRefDTO> mapToSetDTO(Collection<UserRef> entities);

  List<UserRefExportDTO> mapUserRefExportDTOToUserRefDTO(Collection<UserRefDTO> userRefDTOs);

  default Optional<UserRef> mapToOptional(UserRefDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<UserRefDTO> mapToOptional(UserRef entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<UserRefDTO> mapToPageDTO(Page<UserRef> objects) {
    List<UserRefDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default Page<UserRef> mapToPage(Page<UserRefDTO> objects) {
    List<UserRef> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<UserRefDTO> mapToPageResultResource(Page<UserRef> objects) {
    List<UserRefDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
