package cz.cyberrange.platform.training.rest.controllers;

import cz.cyberrange.platform.training.api.dto.run.PublicTrainingCompletionDTO;
import cz.cyberrange.platform.training.api.dto.run.PublicTrainingSummaryDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import cz.cyberrange.platform.training.rest.utils.annotations.ApiPageableSwagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public rest controller for safe training summaries. */
@Api(
    value = "/public/users",
    tags = "Public training summaries",
    consumes = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(
    value = {
      @ApiResponse(
          code = 500,
          message = "Unexpected condition was encountered.",
          response = ApiError.class)
    })
@RestController
@RequestMapping(value = "/public/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class PublicTrainingRunsRestController {

  private final TrainingRunFacade trainingRunFacade;

  public PublicTrainingRunsRestController(TrainingRunFacade trainingRunFacade) {
    this.trainingRunFacade = trainingRunFacade;
  }

  /**
   * Gets public-safe completed training summaries for a user.
   *
   * @param userRefId user reference id.
   * @return public-safe completed training summaries.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get public-safe completed training summaries for a user.",
      response = PublicTrainingCompletionDTO.class,
      responseContainer = "List",
      nickname = "getPublicCompletedTrainings",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Completed training summaries have been found.",
            response = PublicTrainingCompletionDTO.class,
            responseContainer = "List")
      })
  @GetMapping(
      path = "/{userRefId}/completed-trainings",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<PublicTrainingCompletionDTO>> getPublicCompletedTrainings(
      @ApiParam(value = "User reference id.", required = true) @PathVariable("userRefId")
          Long userRefId) {
    return ResponseEntity.ok(trainingRunFacade.findPublicCompletedTrainingRuns(userRefId));
  }

  /**
   * Gets public-safe paged completed training summaries for a user.
   *
   * @param userRefId user reference id.
   * @param pageable pageable parameter with information about pagination.
   * @return public-safe completed training summaries page.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get public-safe paged completed training summaries for a user.",
      response = PublicTrainingCompletionDTO.class,
      responseContainer = "List",
      nickname = "getPublicCompletedTrainingsPage",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Completed training summaries have been found.",
            response = PublicTrainingCompletionDTO.class,
            responseContainer = "List")
      })
  @ApiPageableSwagger
  @GetMapping(
      path = "/{userRefId}/completed-trainings-page",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<PublicTrainingCompletionDTO>> getPublicCompletedTrainingsPage(
      @ApiParam(value = "User reference id.", required = true) @PathVariable("userRefId")
          Long userRefId,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    return ResponseEntity.ok(trainingRunFacade.findPublicCompletedTrainingRuns(userRefId, pageable));
  }

  /**
   * Gets public-safe completed training summary for a user.
   *
   * @param userRefId user reference id.
   * @return public-safe completed training summary.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get public-safe completed training summary for a user.",
      response = PublicTrainingSummaryDTO.class,
      nickname = "getPublicCompletedTrainingSummary",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Completed training summary has been found.",
            response = PublicTrainingSummaryDTO.class)
      })
  @GetMapping(
      path = "/{userRefId}/completed-trainings-summary",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PublicTrainingSummaryDTO> getPublicCompletedTrainingSummary(
      @ApiParam(value = "User reference id.", required = true) @PathVariable("userRefId")
          Long userRefId) {
    return ResponseEntity.ok(trainingRunFacade.findPublicCompletedTrainingSummary(userRefId));
  }
}
