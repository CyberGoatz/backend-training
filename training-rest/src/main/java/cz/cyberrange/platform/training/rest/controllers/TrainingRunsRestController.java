package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.ValidatePasskeyDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.ValidateAnswerDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.rest.utils.annotations.ApiPageableSwagger;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** The rest controller for Training runs. */
@Api(
    value = "/training-runs",
    tags = "Training runs",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          code = 401,
          message = "Full authentication is required to access this resource.",
          response = ApiError.class),
      @ApiResponse(
          code = 403,
          message = "The necessary permissions are required for a resource.",
          response = ApiError.class)
    })
@RestController
@RequestMapping(value = "/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TrainingRunsRestController {

  private TrainingRunFacade trainingRunFacade;
  private ObjectMapper objectMapper;

  /**
   * Instantiates a new Training runs rest controller.
   *
   * @param trainingRunFacade the training run facade
   * @param objectMapper the object mapper
   */
  @Autowired
  public TrainingRunsRestController(
      TrainingRunFacade trainingRunFacade, ObjectMapper objectMapper) {
    this.trainingRunFacade = trainingRunFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Delete training runs.
   *
   * @param trainingRunIds the training run ids
   * @param forceDelete the force delete
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete training runs",
      nickname = "deleteTrainingRuns")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training runs have been deleted."),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered",
            response = ApiError.class)
      })
  @DeleteMapping
  public ResponseEntity<Void> deleteTrainingRuns(
      @ApiParam(value = "Ids of training runs that will be deleted", required = true)
          @RequestParam(value = "trainingRunIds", required = true)
          List<Long> trainingRunIds,
      @ApiParam(
              value =
                  "Indication if this training run must be deleted no matter of any check (force it)",
              required = false)
          @RequestParam(value = "forceDelete", required = false)
          boolean forceDelete) {
    trainingRunFacade.deleteTrainingRuns(trainingRunIds, forceDelete);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Delete a given training run.
   *
   * @param runId the training run id
   * @param forceDelete the force delete
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete training run",
      nickname = "deleteTrainingRun")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training run has been deleted."),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "The training run is still running.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered",
            response = ApiError.class)
      })
  @DeleteMapping(path = "/{runId}")
  public ResponseEntity<Void> deleteTrainingRun(
      @ApiParam(value = "Id of training run that will be deleted", required = true)
          @PathVariable("runId")
          Long runId,
      @ApiParam(
              value =
                  "Indication if this training run must be deleted no matter of any check (force it)",
              required = false)
          @RequestParam(value = "forceDelete", required = false)
          boolean forceDelete) {
    trainingRunFacade.deleteTrainingRun(runId, forceDelete);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Get requested Training Run by id.
   *
   * @param runId of Training Run to return.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested Training Run by id.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get training run by ID.",
      response = TrainingRunByIdDTO.class,
      nickname = "findTrainingRunById",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training run has been found.",
            response = TrainingRunDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingRunById(
      @ApiParam(value = "Id of training run", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingRunByIdDTO trainingRunResource = trainingRunFacade.findById(runId);
    Squiggly.init(objectMapper, fields);
    return new ResponseEntity<>(
        SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
  }

  /**
   * Get all Training Runs.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @return all Training Runs.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all training runs.",
      response = TrainingRunRestResource.class,
      nickname = "findAllTrainingRuns",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training runs have been found.",
            response = TrainingRunRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingRuns(
      @QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    PageResultResource<TrainingRunDTO> trainingRunResource =
        trainingRunFacade.findAll(predicate, pageable);
    Squiggly.init(objectMapper, fields);
    return new ResponseEntity<>(
        SquigglyUtils.stringify(objectMapper, trainingRunResource), HttpStatus.OK);
  }

  /**
   * Access training run.
   *
   * @param accessToken the access token
   * @return first level of training run.
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Access training run.",
      response = AccessTrainingRunDTO.class,
      nickname = "createTrainingRun",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training run has been accessed.",
            response = AccessTrainingRunDTO.class),
        @ApiResponse(
            code = 404,
            message =
                "There is no training instance with given accessToken or first level not found in database.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "No assigned pool to the training instance.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Some error occurred during getting info about sandboxes.",
            response = ApiError.class),
      })
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTrainingRunDTO> accessTrainingRun(
      @ApiParam(value = "accessToken", required = true)
          @RequestParam(value = "accessToken", required = true)
          String accessToken) {
    AccessTrainingRunDTO accessTrainingRunDTO = trainingRunFacade.accessTrainingRun(accessToken);
    return ResponseEntity.ok(accessTrainingRunDTO);
  }

  /**
   * Get all accessed Training Runs.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @param sortByTitle "asc" for ascending alphabetical sort by title, "desc" for descending
   * @return all accessed Training Runs.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all accessed training runs.",
      notes = "Returns training run which was accessed by logged in user",
      response = AccessedTrainingRunRestResource.class,
      nickname = "getAllAccessedTrainingRuns",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The accessed training runs have been found.",
            response = AccessedTrainingRunDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/accessible", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getAllAccessedTrainingRuns(
      @QuerydslPredicate(root = TrainingRun.class) Predicate predicate,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields,
      @ApiParam(
              value = "Sort by title attribute. As values us asc|desc",
              required = false,
              example = "asc")
          @RequestParam(value = "sortByTitle", required = false)
          String sortByTitle) {
    PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOS =
        trainingRunFacade.findAllAccessedTrainingRuns(predicate, pageable, sortByTitle);
    Squiggly.init(objectMapper, fields);
    return new ResponseEntity<>(
        SquigglyUtils.stringify(objectMapper, accessedTrainingRunDTOS), HttpStatus.OK);
  }

  /**
   * Get next level of given Training Run.
   *
   * @param runId of Training Run for which to get next level.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested next level.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get level of given training run.",
      notes =
          "Returns (assessment, training, info) level if any next level exists and training run as well",
      response = AbstractLevelDTO.class,
      nickname = "getNextLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The next level has been found.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The next level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/next-levels", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getNextLevel(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    AbstractLevelDTO levelDTO = trainingRunFacade.getNextLevel(runId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, levelDTO));
  }

  /**
   * Get solution of current training level.
   *
   * @param runId of Training Run for which to get solution.
   * @return Requested solution of training level.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get solution of training level.",
      notes = "Returns solution if given training runs exists and current level is training level",
      response = String.class,
      nickname = "getSolution",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The solution has been found.", response = String.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 400,
            message = "Current level is not training level and does not have solution.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/solutions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getSolution(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId) {
    return ResponseEntity.ok(trainingRunFacade.getSolution(runId));
  }

  /**
   * Get hint of current training level.
   *
   * @param runId of Training Run for which to get hint.
   * @param hintId the hint id
   * @param fields attributes of the object to be returned as the result.
   * @return Requested hint of training level.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get hint of training level.",
      notes = "Returns hint if given training runs exists and current level is training level",
      response = String.class,
      nickname = "getHint",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The hint has been found.", response = HintDTO.class),
        @ApiResponse(
            code = 404,
            message = "The hint has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "The hint with given id is not in current level of training run.",
            response = ApiError.class),
        @ApiResponse(
            code = 400,
            message = "Current level is not training level and does not have hints.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/hints/{hintId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getHint(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Hint ID", required = true) @PathVariable Long hintId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    HintDTO hintDTO = trainingRunFacade.getHint(runId, hintId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, hintDTO));
  }

  /**
   * Check if submitted answer is correct.
   *
   * @param runId the run id
   * @param validateAnswerDTO submitted answer.
   * @return True if answer is correct, false if answer is wrong.
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Check answer of training level",
      notes = "Current level of given training run must be training level",
      response = Boolean.class,
      nickname = "isCorrectAnswer",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The answer has been checked.",
            response = IsCorrectAnswerDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 400,
            message = "Current level is not training level and does not have answer.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{runId}/is-correct-answer", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IsCorrectAnswerDTO> isCorrectAnswer(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Submitted answer", required = true) @RequestBody @Valid
          ValidateAnswerDTO validateAnswerDTO) {
    return ResponseEntity.ok(
        trainingRunFacade.isCorrectAnswer(runId, validateAnswerDTO.getAnswer()));
  }

  /**
   * Check if submitted passkey is correct.
   *
   * @param runId the run id
   * @param validatePasskeyDTO submitted passkey.
   * @return True if passkey is correct, false if passkey is wrong.
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Check passkey of the access level",
      notes = "Current level of given training run must be access level",
      response = Boolean.class,
      nickname = "isCorrectPasskey",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The passkey has been checked.",
            response = Boolean.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 400,
            message = "Current level is not training level and does not have answer.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{runId}/is-correct-passkey", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> isCorrectPasskey(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Submitted passkey", required = true) @RequestBody @Valid
          ValidatePasskeyDTO validatePasskeyDTO) {
    return ResponseEntity.ok(
        trainingRunFacade.isCorrectPasskey(runId, validatePasskeyDTO.getPasskey()));
  }

  /**
   * Resume paused training run.
   *
   * @param runId id of training run.
   * @return current level of training run.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get current level of resumed training run",
      response = AccessTrainingRunDTO.class,
      nickname = "resumeTrainingRun",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training run has been resumed.",
            response = AccessTrainingRunDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot resume finished training run.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/resumption", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTrainingRunDTO> resumeTrainingRun(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId) {
    AccessTrainingRunDTO resumedTrainingRunDTO = trainingRunFacade.resumeTrainingRun(runId);
    return ResponseEntity.ok(resumedTrainingRunDTO);
  }

  /**
   * Finish training run.
   *
   * @param runId id of training run.
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Finish training run",
      nickname = "finishTrainingRun",
      notes =
          "Training run will be finished if the current level is the last level and it is answered.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training run has been finished."),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot finish training run because of the current state.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> finishTrainingRun(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId) {
    trainingRunFacade.finishTrainingRun(runId);
    return ResponseEntity.ok().build();
  }

  /**
   * Evaluate responses to assessment.
   *
   * @param runId id of training run.
   * @param responses to assessment
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Evaluate responses to assessment",
      nickname = "evaluateResponsesToAssessment",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 204,
            message = "The responses to assessment has been evaluated and stored."),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message =
                "Current level of training is not assessment level or level has been already answered.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      value = "/{runId}/assessment-evaluations",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> evaluateResponsesToAssessment(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Responses to assessment", required = true) @Valid @RequestBody
          List<QuestionAnswerDTO> responses) {
    trainingRunFacade.evaluateResponsesToAssessment(runId, responses);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get requested participant of the given training run.
   *
   * @param trainingRunId id of training run for which to get participant
   * @return Participant of specific training run.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get participant.",
      response = UserRefDTO.class,
      nickname = "getParticipant",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The participant has been found.",
            response = UserRefDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/participant", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getParticipant(
      @ApiParam(value = "Get participant for the given runId.") @PathVariable("runId")
          Long trainingRunId) {
    UserRefDTO participant = trainingRunFacade.getParticipant(trainingRunId);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, participant));
  }

  /**
   * Archive training run.
   *
   * @param runId id of training run.
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PATCH",
      value = "Archive training run",
      nickname = "archiveTrainingRun",
      notes = "The state of the Training run will be change to archived.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training run has been archived."),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PatchMapping(path = "/{runId}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> archiveTrainingRun(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId) {
    trainingRunFacade.archiveTrainingRun(runId);
    return ResponseEntity.ok().build();
  }

  /**
   * Get correct answers of all training levels for the specific training run.
   *
   * @param runId of Training Run for which to get correct answers.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested correct answers of the training run.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get correct answers of the training run.",
      notes =
          "Returns non-empty list of answers if given training run exists and contains at least one training level",
      response = CorrectAnswerDTO[].class,
      nickname = "getCorrectAnswers",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The correct answers have been found.",
            response = CorrectAnswerDTO[].class),
        @ApiResponse(
            code = 404,
            message = "The training run has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/answers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getCorrectAnswers(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    List<CorrectAnswerDTO> correctAnswerDTOs = trainingRunFacade.getCorrectAnswers(runId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, correctAnswerDTOs));
  }

  /**
   * Get previous or current level (any visited) of given Training Run.
   *
   * @param runId of Training Run for which to get previous or current level.
   * @param levelId ID of the visited level.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested level.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get visited level of given training run.",
      notes =
          "Returns (assessment, training, info) level if any level exists and training run as well",
      response = AbstractLevelDTO.class,
      nickname = "getVisitedLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The visited level has been found.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The visited level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{runId}/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getVisitedLevel(
      @ApiParam(value = "Training run ID", required = true) @PathVariable("runId") Long runId,
      @ApiParam(value = "Level ID", required = true) @PathVariable("levelId") Long levelId,
      @ApiParam(value = "Fields which should be returned in REST API response")
          @RequestParam(value = "fields", required = false)
          String fields) {
    AbstractLevelDTO levelDTO = trainingRunFacade.getVisitedLevel(runId, levelId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, levelDTO));
  }

  /** The type Training run rest resource. */
  @ApiModel(
      value = "TrainingRunRestResource",
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  public static class TrainingRunRestResource extends PageResultResource<TrainingRunDTO> {
    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Runs from databases.")
    private List<TrainingRunDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }

  @ApiModel(
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  private static class AccessedTrainingRunRestResource
      extends PageResultResource<AccessedTrainingRunDTO> {
    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Accessed Training Runs from databases.")
    private List<AccessedTrainingRunDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }
}
