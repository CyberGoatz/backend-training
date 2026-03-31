package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
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
 * The TrainingRunMapper is an utility class to map items into data transfer objects. It provides
 * the implementation of mappings between Java bean type TrainingRunMapper and DTOs classes. Code is
 * generated during compile time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingRunMapper extends ParentMapper {
  TrainingRun mapToEntity(TrainingRunDTO dto);

  TrainingRunDTO mapToDTO(TrainingRun entity);

  TrainingRunByIdDTO mapToFindByIdDTO(TrainingRun entity);

  List<TrainingRun> mapToList(Collection<TrainingRunDTO> dtos);

  List<TrainingRunDTO> mapToListDTO(Collection<TrainingRun> entities);

  Set<TrainingRun> mapToSet(Collection<TrainingRunDTO> dtos);

  Set<TrainingRunDTO> mapToSetDTO(Collection<TrainingRun> entities);

  default Optional<TrainingRun> mapToOptional(TrainingRunDTO dto) {
    return Optional.ofNullable(mapToEntity(dto));
  }

  default Optional<TrainingRunDTO> mapToOptional(TrainingRun entity) {
    return Optional.ofNullable(mapToDTO(entity));
  }

  default Page<TrainingRunDTO> mapToPageDTO(Page<TrainingRun> objects) {
    List<TrainingRunDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default Page<TrainingRun> mapToPage(Page<TrainingRunDTO> objects) {
    List<TrainingRun> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<TrainingRunDTO> mapToPageResultResource(Page<TrainingRun> objects) {
    List<TrainingRunDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  default PageResultResource<TrainingRunDTO> mapToPageResultResourceLogging(
      Page<TrainingRun> objects, Set<Long> eventLoggingIds, Set<Long> commandLoggingIds) {
    List<TrainingRunDTO> mapped = new ArrayList<>();
    objects.forEach(
        object -> {
          TrainingRunDTO runDTO = mapToDTO(object);
          runDTO.setEventLoggingState(eventLoggingIds.contains(runDTO.getId()));
          runDTO.setCommandLoggingState(commandLoggingIds.contains(runDTO.getId()));
          mapped.add(runDTO);
        });
    return new PageResultResource<>(mapped, createPagination(objects));
  }

  default PageResultResource<AccessedTrainingRunDTO> mapToPageResultResourceAccessed(
      Page<AccessedTrainingRunDTO> objects) {
    List<AccessedTrainingRunDTO> mapped = new ArrayList<>();
    objects.forEach(mapped::add);
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
