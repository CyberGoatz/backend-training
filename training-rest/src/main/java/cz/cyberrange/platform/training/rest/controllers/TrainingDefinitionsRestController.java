package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.commons.security.mapping.UserInfoDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.visualization.VisualizationInfoDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.enums.LevelType;
import cz.cyberrange.platform.training.rest.utils.annotations.ApiPageableSwagger;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The rest controller for Training definitions. */
@Api(
    value = "/training-definitions",
    tags = "Training definitions",
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
@Validated
@RestController
@RequestMapping(path = "/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingDefinitionsRestController {

  private TrainingDefinitionFacade trainingDefinitionFacade;
  private ObjectMapper objectMapper;

  /**
   * Instantiates a new Training Definitions rest controller.
   *
   * @param trainingDefinitionFacade the training definition facade
   * @param objectMapper the object mapper
   */
  @Autowired
  public TrainingDefinitionsRestController(
      TrainingDefinitionFacade trainingDefinitionFacade, ObjectMapper objectMapper) {
    this.trainingDefinitionFacade = trainingDefinitionFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Get requested Training Definition by id.
   *
   * @param id of Training Definition to return.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested Training Definition by id.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get Training Definition by Id.",
      response = TrainingDefinitionByIdDTO.class,
      nickname = "findTrainingDefinitionById",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been found.",
            response = TrainingDefinitionByIdDTO.class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingDefinitionById(
      @ApiParam(value = "ID of training definition to be retrieved.", required = true)
          @PathVariable(value = "definitionId")
          Long id,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingDefinitionByIdDTO trainingDefinitionResource = trainingDefinitionFacade.findById(id);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Get all Training Definitions.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @return all Training Definitions.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all Training Definitions.",
      response = TrainingDefinitionRestResource.class,
      nickname = "findAllTrainingDefinitions",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The requested resources have been found.",
            response = TrainingDefinitionByIdDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitions(
      @QuerydslPredicate(root = TrainingDefinition.class) Predicate predicate,
      Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {

    PageResultResource<TrainingDefinitionDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAll(predicate, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Get all Training Definitions for organizers.
   *
   * @param state training definition state (should be RELEASED or UNRELEASED)
   * @param pageable pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @return all Training Definitions for organizers.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all Training Definitions for organizers.",
      response = TrainingDefinitionRestResource.class,
      nickname = "findAllTrainingDefinitionsForOrganizers",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definitions have been found.",
            response = TrainingDefinitionInfoDTO.class,
            responseContainer = "List"),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/for-organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingDefinitionsForOrganizers(
      @ApiParam(value = "State of the training definition", required = true)
          @RequestParam(value = "state")
          TDState state,
      Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {

    PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionResource =
        trainingDefinitionFacade.findAllForOrganizers(state, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Create Training Definition.
   *
   * @param trainingDefinitionCreateDTO the Training Definition to be create
   * @param fields attributes of the object to be returned as the result.
   * @return the new Training Definition
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Create Training Definition",
      response = TrainingDefinitionByIdDTO.class,
      nickname = "createTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been created.",
            response = TrainingDefinitionByIdDTO.class),
        @ApiResponse(
            code = 400,
            message = "The provided training definition is not valid",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTrainingDefinition(
      @ApiParam(value = "Training Definition to be created") @RequestBody @Valid
          TrainingDefinitionCreateDTO trainingDefinitionCreateDTO,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingDefinitionByIdDTO trainingDefinitionResource =
        trainingDefinitionFacade.create(trainingDefinitionCreateDTO);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
  }

  /**
   * Update Training Definition.
   *
   * @param trainingDefinitionUpdateDTO the training definition to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update Training Definition",
      notes = "Only unreleased training definition can be updated",
      nickname = "updateTrainingDefinition",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training definition has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training definition is not valid",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingDefinition(
      @ApiParam(value = "Training definition to be updated") @RequestBody @Valid
          TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
    trainingDefinitionFacade.update(trainingDefinitionUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Clone Training Definition response entity.
   *
   * @param id the id of cloned Training Definition
   * @param title the title of new Training Definition
   * @return the new Training Definition
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Clone training definition",
      notes = "Only released and archived training definitions can be cloned",
      response = TrainingDefinitionByIdDTO.class,
      nickname = "cloneTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The Training definition has been cloned.",
            response = TrainingDefinitionByIdDTO.class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{definitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrainingDefinitionByIdDTO> cloneTrainingDefinition(
      @ApiParam(value = "Id of training definition to be cloned", required = true)
          @PathVariable("definitionId")
          Long id,
      @ApiParam(value = "Title of cloned definition", required = true)
          @RequestParam(value = "title")
          String title) {
    TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionFacade.clone(id, title);
    return ResponseEntity.ok(trainingDefinitionByIdDTO);
  }

  /**
   * Swap levels.
   *
   * @param definitionId the Training Definition id
   * @param levelIdFrom the level id from
   * @param levelIdTo the level id to
   * @return the basic information about levels
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Swap levels",
      notes = "The first one level cannot be swapped to the left",
      nickname = "swapLevels",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = BasicLevelInfoDTO[].class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The level has been swapped to the left.",
            response = BasicLevelInfoDTO[].class),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message =
                "Cannot edit released or archived training definition or cannot swap first level to the left.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> swapLevels(
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("levelIdFrom")
          Long levelIdFrom,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("levelIdTo")
          Long levelIdTo) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.swapLevels(definitionId, levelIdFrom, levelIdTo));
  }

  /**
   * Move the given level to the specified position.
   *
   * @param definitionId the Training Definition id
   * @param levelIdToBeMoved the level id from
   * @param newPosition position where move the given level
   * @return the basic information about levels
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Move level",
      nickname = "moveLevel",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = BasicLevelInfoDTO.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The level has been moved to the given position."),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/levels/{levelIdToBeMoved}/move-to/{newPosition}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> moveLevel(
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of training definition", required = true)
          @PathVariable("levelIdToBeMoved")
          Long levelIdToBeMoved,
      @ApiParam(value = "Id of training definition", required = true) @PathVariable("newPosition")
          Integer newPosition) {
    return ResponseEntity.ok(
        trainingDefinitionFacade.moveLevel(definitionId, levelIdToBeMoved, newPosition));
  }

  /**
   * Delete Training Definition.
   *
   * @param id the id of definition to be deleted
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete training definition",
      notes = "Released training definition cannot be deleted",
      nickname = "deleteTrainingDefinition")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The Training definition has been deleted."),
        @ApiResponse(
            code = 404,
            message = "The Training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot delete released training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @DeleteMapping(path = "/{definitionId}")
  public ResponseEntity<Void> deleteTrainingDefinition(
      @ApiParam(value = "Id of training definition to be deleted", required = true)
          @PathVariable("definitionId")
          Long id) {
    trainingDefinitionFacade.delete(id);
    return ResponseEntity.ok().build();
  }

  /**
   * Delete one level from Training Definition.
   *
   * @param definitionId the Training Definition id
   * @param levelId the level id
   * @return the basic information about levels
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete specific level from training definition",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "deleteOneLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The level has been deleted."),
        @ApiResponse(
            code = 404,
            message = "The level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @DeleteMapping(
      path = "/{definitionId}/levels/{levelId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> deleteOneLevel(
      @ApiParam(value = "Id of training definition from which level is deleted", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Id of level to be deleted", required = true) @PathVariable("levelId")
          Long levelId) {
    return ResponseEntity.ok(trainingDefinitionFacade.deleteOneLevel(definitionId, levelId));
  }

  /**
   * Update training level.
   *
   * @param definitionId the Training Definition id
   * @param trainingLevelUpdateDTO the training level to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update training level",
      notes = "Level can be updated only in unreleased training definition",
      nickname = "updateTrainingLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The training level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/training-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateTrainingLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Training level to be updated") @RequestBody @Valid
          TrainingLevelUpdateDTO trainingLevelUpdateDTO) {
    trainingDefinitionFacade.updateTrainingLevel(definitionId, trainingLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Update info level.
   *
   * @param definitionId the definition id
   * @param infoLevelUpdateDTO the info level to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update info level",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "updateInfoLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The info level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided info level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The info level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/info-levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateInfoLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Info level to be updated") @RequestBody @Valid
          InfoLevelUpdateDTO infoLevelUpdateDTO) {
    trainingDefinitionFacade.updateInfoLevel(definitionId, infoLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Update assessment level.
   *
   * @param definitionId the definition id
   * @param assessmentLevelUpdateDTO the assessment level to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update assessment level",
      notes = "Level can be deleted only in unreleased training definition",
      nickname = "updateAssessmentLevel",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The assessment level has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided assessment level is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The level has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      path = "/{definitionId}/assessment-levels",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateAssessmentLevel(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Assessment level to be updated") @RequestBody @Valid
          AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
    trainingDefinitionFacade.updateAssessmentLevel(definitionId, assessmentLevelUpdateDTO);
    return ResponseEntity.noContent().build();
  }

  /**
   * Update levels.
   *
   * @param definitionId the definition id
   * @param levelUpdateDTOS the levels to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update levels",
      notes = "Levels can be updated only in unreleased training definition.",
      nickname = "updateLevels",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The levels has been updated."),
        @ApiResponse(
            code = 400,
            message = "One of the provided levels is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "One of the provided levels has not been found in definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/levels", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateLevels(
      @ApiParam(value = "Id of definition to which level is assigned", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(value = "Levels to be updated") @RequestBody @Valid
          List<AbstractLevelUpdateDTO> levelUpdateDTOS) {
    trainingDefinitionFacade.updateLevels(definitionId, levelUpdateDTOS);
    return ResponseEntity.noContent().build();
  }

  /**
   * Find level by id.
   *
   * @param levelId the id of wanted level
   * @param fields attributes of the object to be returned as the result.
   * @return wanted level
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Find level by ID",
      response = AbstractLevelDTO.class,
      nickname = "findLevelById",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The level has been found.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The level has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findLevelById(
      @ApiParam(value = "Id of wanted level", required = true) @PathVariable("levelId")
          Long levelId,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    AbstractLevelDTO level = trainingDefinitionFacade.findLevelById(levelId);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, level));
  }

  /**
   * Create level.
   *
   * @param definitionId the definition id
   * @param levelType the type of new level
   * @param fields attributes of the object to be returned as the result.
   * @return the basic information about new level
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Create level",
      notes = "Creates only default level for given training definition",
      response = BasicLevelInfoDTO.class,
      nickname = "createLevel",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 201,
            message = "The level has been created.",
            response = AbstractLevelDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been not found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot create level in released or archived training definition.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(path = "/{definitionId}/levels/{levelType}")
  public ResponseEntity<Object> createLevel(
      @ApiParam(value = "Id of definition for which is level created", required = true)
          @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(
              value = "Level type",
              allowableValues = "TRAINING, ASSESSMENT, INFO",
              required = true)
          @PathVariable("levelType")
          LevelType levelType,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    BasicLevelInfoDTO basicLevelInfoDTO;
    if (levelType.equals(LevelType.TRAINING)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createTrainingLevel(definitionId);
    } else if (levelType.equals(LevelType.ASSESSMENT)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createAssessmentLevel(definitionId);
    } else if (levelType.equals(LevelType.ACCESS)) {
      basicLevelInfoDTO = trainingDefinitionFacade.createAccessLevel(definitionId);
    } else {
      basicLevelInfoDTO = trainingDefinitionFacade.createInfoLevel(definitionId);
    }
    Squiggly.init(objectMapper, fields);
    return new ResponseEntity<>(
        SquigglyUtils.stringify(objectMapper, basicLevelInfoDTO), HttpStatus.CREATED);
  }

  /**
   * Get requested designers.
   *
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role designer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get designers.",
      response = UserInfoRestResource.class,
      nickname = "getDesigners",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The designers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/designers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getDesigners(
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getUsersWithGivenRole(
            RoleType.ROLE_TRAINING_DESIGNER, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Get requested organizers.
   *
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role designer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get organizers.",
      response = UserInfoRestResource.class,
      nickname = "getOrganizers",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The organizers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getOrganizers(
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> organizers =
        trainingDefinitionFacade.getUsersWithGivenRole(
            RoleType.ROLE_TRAINING_ORGANIZER, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, organizers));
  }

  /**
   * Get requested designers not in given Training Definition.
   *
   * @param trainingDefinitionId id of the training definition
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role designer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get designers not in given training definition.",
      response = UserInfoRestResource.class,
      nickname = "findDesignersNotInGivenTrainingDefinition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The designers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(
      path = "{definitionId}/designers-not-in-training-definition",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getDesignersNotInGivenTrainingDefinition(
      @ApiParam(
              value =
                  "ID of the training definition which do not contains authors you want to retrieve.",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
            trainingDefinitionId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Get requested beta testers for Training Definition.
   *
   * @param trainingDefinitionId id of training definition for which to get beta testers
   * @param pageable pageable parameter with information about pagination.
   * @return List of beta testers and theirs info.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get beta testers.",
      response = UserInfoRestResource.class,
      nickname = "getBetaTesters",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The beta testers have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/{definitionId}/beta-testers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getBetaTesters(
      @ApiParam(
              value =
                  "ID of the training definition which contains beta testers you want to retrieve",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getBetaTesters(trainingDefinitionId, pageable);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Get requested authors for Training Definition.
   *
   * @param trainingDefinitionId id of training definition for which to retrieve authors
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role designer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get authors.",
      response = UserInfoRestResource.class,
      nickname = "getAuthors",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The authors have been found.",
            response = UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getAuthors(
      @ApiParam(
              value = "ID of the training definition which contains authors you want to retrieve.",
              required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingDefinitionFacade.getAuthors(trainingDefinitionId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Concurrently add/remove authors with given ids to/from the Training Definition.
   *
   * @param trainingDefinitionId id of training definition for which to retrieve authors
   * @param authorsAddition ids of the authors to be added to the training definition.
   * @param authorsRemoval ids of the authors to be removed from the training definition.
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Edit authors.",
      response = UserInfoRestResource.class,
      nickname = "editAuthors",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The authors have been updated."),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @PutMapping(path = "/{definitionId}/authors", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> editAuthors(
      @ApiParam(value = "ID of training definition to be updated.", required = true)
          @PathVariable("definitionId")
          Long trainingDefinitionId,
      @ApiParam(value = "Ids of the users to be added to the training definition.")
          @RequestParam(value = "authorsAddition", required = false)
          Set<Long> authorsAddition,
      @ApiParam(value = "Ids of the users to be removed from the training definition.")
          @RequestParam(value = "authorsRemoval", required = false)
          Set<Long> authorsRemoval) {
    trainingDefinitionFacade.editAuthors(trainingDefinitionId, authorsAddition, authorsRemoval);
    return ResponseEntity.noContent().build();
  }

  /**
   * Switch development state of given definition.
   *
   * @param definitionId the definition id
   * @param state the new development state
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Switch state of training definition",
      nickname = "switchDefinitionState")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training definition has been updated."),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "Cannot edit definition with created instances.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(path = "/{definitionId}/states/{state}")
  public ResponseEntity<Void> switchState(
      @ApiParam(value = "Id of definition", required = true) @PathVariable("definitionId")
          Long definitionId,
      @ApiParam(
              value = "New state of definition",
              allowableValues = "RELEASED, UNRELEASED, ARCHIVED",
              required = true)
          @PathVariable("state")
          TDState state) {
    trainingDefinitionFacade.switchState(definitionId, state);
    return ResponseEntity.noContent().build();
  }

  /**
   * Check if the reference solution is defined for the given training definition.
   *
   * @param definitionId the training definition id
   * @return true if at least one of the training levels has reference solution defined, false
   *     otherwise.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get boolean value if the reference solution is defined or not.",
      response = Boolean.class,
      nickname = "hasReferenceSolution",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "True - reference solution is defined, false - otherwise.",
            response = VisualizationInfoDTO.class),
        @ApiResponse(
            code = 404,
            message = "Training definition not found.",
            response = ApiError.class)
      })
  @GetMapping(
      path = "/{definitionId}/has-reference-solution",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> hasReferenceSolution(
      @ApiParam(value = "Training Definition ID", required = true) @PathVariable("definitionId")
          Long definitionId) {
    return ResponseEntity.ok(trainingDefinitionFacade.hasReferenceSolution(definitionId));
  }

  @ApiModel(
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  private static class TrainingDefinitionRestResource
      extends PageResultResource<TrainingDefinitionByIdDTO> {

    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Definitions from databases.")
    private List<TrainingDefinitionByIdDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }

  /** The type User info rest resource. */
  @ApiModel(
      value = "UserInfoRestResource",
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  public static class UserInfoRestResource extends PageResultResource<UserInfoDTO> {
    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Instances from databases.")
    private List<UserRefDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }
}
