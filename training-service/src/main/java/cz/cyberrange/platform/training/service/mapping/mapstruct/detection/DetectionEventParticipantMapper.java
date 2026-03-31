package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectionEventParticipantMapper extends ParentMapper {
  DetectionEventParticipant mapToEntity(DetectionEventParticipantDTO dto);

  DetectionEventParticipantDTO mapToDTO(DetectionEventParticipant entity);

  List<DetectionEventParticipant> mapToList(Collection<DetectionEventParticipantDTO> dtos);

  List<DetectionEventParticipantDTO> mapToListDTO(Collection<DetectionEventParticipant> entities);

  default Page<DetectionEventParticipant> mapToPage(Page<DetectionEventParticipantDTO> objects) {
    List<DetectionEventParticipant> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<DetectionEventParticipantDTO> mapToPageResultResource(
      Page<DetectionEventParticipant> objects) {
    List<DetectionEventParticipantDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
