package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MitreTechniqueMapper {

  MitreTechnique mapToEntity(MitreTechniqueDTO dto);

  MitreTechniqueDTO mapToDTO(MitreTechnique dto);

  @Named("ignoreId")
  @Mapping(target = "id", ignore = true)
  MitreTechniqueDTO mapToDTOIgnoreId(MitreTechnique dto);

  List<MitreTechnique> mapDTOsToList(Collection<MitreTechniqueDTO> dtos);

  List<MitreTechniqueDTO> mapToListDTO(Collection<MitreTechnique> entities);

  @Named("ignoreIds")
  @IterableMapping(qualifiedByName = "ignoreId")
  Set<MitreTechniqueDTO> mapToListDTOIgnoreIds(Collection<MitreTechnique> entities);
}
