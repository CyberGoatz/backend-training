package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.AbstractDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AnswerSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.LocationSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.MinimalSolveTimeDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.NoCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.TimeProximityDetectionEventDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * The CheatMapper is an utility class to map items into data transfer objects. It provides the
 * implementation of mappings between Java bean type CheatMapper and DTOs classes. Code is generated
 * during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {DetectionEventParticipant.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectionEventMapper extends ParentMapper {
  // ANSWER SIMILARITY
  AnswerSimilarityDetectionEvent mapToEntity(AnswerSimilarityDetectionEventDTO dto);

  AnswerSimilarityDetectionEventDTO mapToAnswerSimilarityDetectionEventDTO(
      AnswerSimilarityDetectionEvent entity);

  // LOCATION SIMILARITY
  LocationSimilarityDetectionEvent mapToEntity(LocationSimilarityDetectionEventDTO dto);

  LocationSimilarityDetectionEventDTO mapToLocationSimilarityDetectionEventDTO(
      LocationSimilarityDetectionEvent entity);

  // TIME PROXIMITY
  TimeProximityDetectionEvent mapToEntity(TimeProximityDetectionEventDTO dto);

  TimeProximityDetectionEventDTO mapToTimeProximityDetectionEventDTO(
      TimeProximityDetectionEvent entity);

  // MINIMAL SOLVE TIME
  MinimalSolveTimeDetectionEvent mapToEntity(MinimalSolveTimeDetectionEventDTO dto);

  MinimalSolveTimeDetectionEventDTO mapToMinimalSolveTimeDetectionEventDTO(
      MinimalSolveTimeDetectionEvent entity);

  // FORBIDDEN COMMANDS
  ForbiddenCommandsDetectionEvent mapToEntity(ForbiddenCommandsDetectionEventDTO dto);

  ForbiddenCommandsDetectionEventDTO mapToForbiddenCommandsDetectionEventDTO(
      ForbiddenCommandsDetectionEvent entity);

  // NO COMMANDS
  NoCommandsDetectionEvent mapToEntity(NoCommandsDetectionEventDTO dto);

  NoCommandsDetectionEventDTO mapToNoCommandsDetectionEventDTO(NoCommandsDetectionEvent entity);

  // ABSTRACT
  AbstractDetectionEventDTO mapToDTO(AbstractDetectionEvent entity);

  List<AbstractDetectionEventDTO> mapToList(Collection<AbstractDetectionEventDTO> dtos);

  List<AbstractDetectionEventDTO> mapToListDTO(Collection<AbstractDetectionEventDTO> entities);

  Set<AbstractDetectionEventDTO> mapToSet(Collection<AbstractDetectionEventDTO> dtos);

  Set<AbstractDetectionEventDTO> mapToSetDTO(Collection<AbstractDetectionEventDTO> entities);

  default Page<AbstractDetectionEventDTO> mapToPageDTO(Page<AbstractDetectionEventDTO> objects) {
    List<AbstractDetectionEventDTO> mapped = mapToListDTO(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default Page<AbstractDetectionEventDTO> mapToPage(Page<AbstractDetectionEventDTO> objects) {
    List<AbstractDetectionEventDTO> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<AbstractDetectionEventDTO> mapToPageResultResource(
      Page<AbstractDetectionEvent> objects) {
    List<AbstractDetectionEventDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
