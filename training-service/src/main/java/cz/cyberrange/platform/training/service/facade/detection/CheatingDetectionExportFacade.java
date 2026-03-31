package cz.cyberrange.platform.training.service.facade.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.CheatingDetectionMapper;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.detection.CheatingDetectionExportService;
import cz.cyberrange.platform.training.service.services.detection.CheatingDetectionService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The type Cheating Detection facade. */
@Service
@Transactional
public class CheatingDetectionExportFacade {

  private final CheatingDetectionService cheatingDetectionService;

  public final UserService userService;

  private final CheatingDetectionMapper cheatingDetectionMapper;
  private final CheatingDetectionExportService cheatingDetectionExportService;

  /**
   * Instantiates a new Cheating detection facade.
   *
   * @param cheatingDetectionService the cheating detection service
   * @param userService the user service
   * @param cheatingDetectionMapper the cheating detection mapper
   * @param cheatingDetectionExportService the cheating detection export service
   */
  @Autowired
  public CheatingDetectionExportFacade(
      CheatingDetectionService cheatingDetectionService,
      UserService userService,
      CheatingDetectionMapper cheatingDetectionMapper,
      CheatingDetectionExportService cheatingDetectionExportService) {
    this.cheatingDetectionService = cheatingDetectionService;
    this.userService = userService;
    this.cheatingDetectionMapper = cheatingDetectionMapper;
    this.cheatingDetectionExportService = cheatingDetectionExportService;
  }

  /**
   * Exports Cheating Detection to file
   *
   * @param cheatingDetectionId the id of the cheating detection to be exported
   * @return the file containing cheating detection, {@link FileToReturnDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
  @TransactionalRO
  public FileToReturnDTO archiveCheatingDetectionResults(Long cheatingDetectionId) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {
      CheatingDetection cheatingDetection =
          cheatingDetectionService.findCheatingDetectionById(cheatingDetectionId);
      CheatingDetectionDTO cheatingDetectionDTO =
          cheatingDetectionMapper.mapToDTO(cheatingDetection);

      cheatingDetectionExportService.writeCheatingDetection(
          zos, cheatingDetectionId, cheatingDetectionDTO);
      cheatingDetectionExportService.writeAnswerSimilarityDetectionEvents(zos, cheatingDetectionId);
      cheatingDetectionExportService.writeLocationSimilarityDetectionEvents(
          zos, cheatingDetectionId);
      cheatingDetectionExportService.writeTimeProximityDetectionEvents(zos, cheatingDetectionId);
      cheatingDetectionExportService.writeMinimalSolveTimeDetectionEvents(zos, cheatingDetectionId);
      cheatingDetectionExportService.writeNoCommandsDetectionEvents(zos, cheatingDetectionId);
      cheatingDetectionExportService.writeForbiddenCommandsDetectionEvents(
          zos, cheatingDetectionId);
      cheatingDetectionExportService.writeTraineeParticipantGroups(zos, cheatingDetectionId);

      zos.closeEntry();
      zos.close();
      FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
      fileToReturnDTO.setContent(baos.toByteArray());
      fileToReturnDTO.setTitle("cheating-detection-" + cheatingDetection.getId().toString());
      return fileToReturnDTO;
    } catch (IOException ex) {
      throw new InternalServerErrorException(
          "The .zip file was not created since there were some processing error.", ex);
    }
  }
}
