package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.export.AttachmentExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AttachmentImportDTO;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachmentMapper {

  Attachment mapImportDTOToEntity(AttachmentImportDTO dto);

  AttachmentExportDTO mapToExportDTO(Attachment entity);

  List<Attachment> mapImportDTOsToList(Collection<AttachmentImportDTO> dtos);

  List<AttachmentExportDTO> mapToListExportDTO(Collection<Attachment> entities);
}
