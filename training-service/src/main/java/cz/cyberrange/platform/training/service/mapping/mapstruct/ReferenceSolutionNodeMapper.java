package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.persistence.model.ReferenceSolutionNode;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReferenceSolutionNodeMapper extends ParentMapper {

  ReferenceSolutionNodeMapper INSTANCE = Mappers.getMapper(ReferenceSolutionNodeMapper.class);

  List<ReferenceSolutionNode> mapToList(List<ReferenceSolutionNodeDTO> dtos);

  ReferenceSolutionNode mapToEntity(ReferenceSolutionNodeDTO dto);

  List<ReferenceSolutionNodeDTO> mapToListDTO(List<ReferenceSolutionNode> entities);

  Set<ReferenceSolutionNodeDTO> mapToSetDTO(Set<ReferenceSolutionNode> entities);

  ReferenceSolutionNodeDTO mapToDTO(ReferenceSolutionNode entity);
}
