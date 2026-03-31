package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.archive.TrainingRunArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.TrainingRunExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * The ExportImportMapper is an utility class to map items into data transfer objects. It provides
 * the implementation of mappings between Java bean type ExportImportMapper and DTOs classes. Code
 * is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    uses = {UserRefMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExportImportMapper extends ParentMapper {

  ExportTrainingDefinitionAndLevelsDTO mapToDTO(TrainingDefinition entity);

  TrainingDefinition mapToEntity(ImportTrainingDefinitionDTO dto);

  TrainingInstanceArchiveDTO mapToDTO(TrainingInstance entity);

  TrainingRunExportDTO mapToDTO(TrainingRun entity);

  TrainingRunArchiveDTO mapToArchiveDTO(TrainingRun entity);
}
