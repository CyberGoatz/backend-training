package cz.cyberrange.platform.training.service.mapping.mapstruct.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ParentMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectedForbiddenCommandMapper extends ParentMapper {

  DetectedForbiddenCommand mapToEntity(DetectedForbiddenCommandDTO dto);

  DetectedForbiddenCommandDTO mapToDTO(DetectedForbiddenCommand entity);

  List<DetectedForbiddenCommand> mapToList(Collection<DetectedForbiddenCommandDTO> dtos);

  List<DetectedForbiddenCommandDTO> mapToListDTO(Collection<DetectedForbiddenCommand> entities);

  default Page<DetectedForbiddenCommand> mapToPage(Page<DetectedForbiddenCommandDTO> objects) {
    List<DetectedForbiddenCommand> mapped = mapToList(objects.getContent());
    return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
  }

  default PageResultResource<DetectedForbiddenCommandDTO> mapToPageResultResource(
      Page<DetectedForbiddenCommand> objects) {
    List<DetectedForbiddenCommandDTO> mapped = new ArrayList<>();
    objects.forEach(object -> mapped.add(mapToDTO(object)));
    return new PageResultResource<>(mapped, createPagination(objects));
  }
}
