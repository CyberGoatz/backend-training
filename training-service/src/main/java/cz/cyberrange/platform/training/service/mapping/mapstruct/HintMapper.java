package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.export.HintExportDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * The HintMapper is an utility class to map items into data transfer objects. It provides the
 * implementation of mappings between Java bean type HintMapper and DTOs classes. Code is generated
 * during compile time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HintMapper extends ParentMapper {

  Hint mapToEntity(HintDTO dto);

  HintDTO mapToDTO(Hint entity);

  @Mapping(source = "hintId", target = "id")
  @Mapping(source = "hintContent", target = "content")
  @Mapping(source = "hintTitle", target = "title")
  TakenHintDTO mapToDTO(HintInfo hintInfo);

  HintExportDTO mapToHintExportDTO(Hint entity);

  List<Hint> mapToList(Collection<HintDTO> dtos);

  List<HintDTO> mapToListDTO(Collection<Hint> entities);

  Set<Hint> mapToSet(Collection<HintDTO> dtos);

  Set<HintDTO> mapToSetDTO(Collection<Hint> entities);

  Set<TakenHintDTO> mapToSetInfoDTO(Collection<HintInfo> entities);

  default Optional<Hint> mapToOptional(HintDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<HintDTO> mapToOptional(Hint entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<HintDTO> mapToPageDTO(Page<Hint> objects) {
    List<HintDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default Page<Hint> mapToPage(Page<HintDTO> objects) {
    List<Hint> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<HintDTO> mapToPageResultResource(Page<Hint> objects) {
    List<HintDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
