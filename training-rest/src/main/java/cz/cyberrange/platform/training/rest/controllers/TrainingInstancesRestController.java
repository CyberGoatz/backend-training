package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.rest.utils.annotations.ApiPageableSwagger;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.TrainingInstanceFacade;
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
import org.springframework.web.bind.annotation.*;

/** The rest controller for Training instances. */
@Api(
    value = "/training-instances",
    tags = "Training instances",
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
@RequestMapping(path = "/training-instances", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainingInstancesRestController {

  private TrainingInstanceFacade trainingInstanceFacade;
  private ObjectMapper objectMapper;

  /**
   * Instantiates a new Training instances rest controller.
   *
   * @param trainingInstanceFacade the training instance facade
   * @param objectMapper the object mapper
   */
  @Autowired
  public TrainingInstancesRestController(
      TrainingInstanceFacade trainingInstanceFacade, ObjectMapper objectMapper) {
    this.trainingInstanceFacade = trainingInstanceFacade;
    this.objectMapper = objectMapper;
  }

  /**
   * Get requested Training Instance by id.
   *
   * @param id id of the Training Instance to return.
   * @param fields attributes of the object to be returned as the result.
   * @return Requested Training Instance by id.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get training instance by id.",
      response = TrainingInstanceDTO.class,
      nickname = "findTrainingInstanceById",
      notes =
          "Returns training instance by id and also contains particular training definition in it.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training instance has been found",
            response = TrainingInstanceDTO.class),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findTrainingInstanceById(
      @ApiParam(value = "Training instance ID", required = true) @PathVariable("instanceId")
          Long id,
      @ApiParam(value = "Fields which should be returned in REST API response")
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingInstanceDTO trainingInstanceResource = trainingInstanceFacade.findById(id);
    return ResponseEntity.ok(trainingInstanceResource);
  }

  /**
   * Get Training instance access token by pool id.
   *
   * @param poolId id of the assigned pool.
   * @return Requested access token by pool id if it exists.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get training instance access token by pool id.",
      response = String.class,
      nickname = "findTrainingInstanceAccessTokenByPoolId",
      notes = "Returns training instance access token by pool id.",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The access token has been found",
            response = TrainingInstanceDTO.class),
        @ApiResponse(
            code = 404,
            message = "The access token has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/access/{poolId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findInstanceAccessTokenByPoolId(
      @ApiParam(value = "Pool ID", required = true) @PathVariable("poolId") Long poolId) {
    String accessToken = trainingInstanceFacade.findInstanceAccessTokenByPoolId(poolId);
    return ResponseEntity.ok(accessToken);
  }

  /**
   * Get all Training Instances.
   *
   * @param predicate specifies query to database.
   * @param pageable pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @return all Training Instances.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all training instances.",
      response = TrainingInstanceRestResource.class,
      nickname = "findAllTrainingInstances",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training instances have been found.",
            response = TrainingInstanceRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingInstances(
      @QuerydslPredicate(root = TrainingInstance.class) Predicate predicate,
      @ApiParam(value = "Pagination support.", required = false) Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceResource =
        trainingInstanceFacade.findAll(predicate, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
  }

  /**
   * Create new Training Instance.
   *
   * @param trainingInstanceCreateDTO the Training Instance to be created
   * @param fields attributes of the object to be returned as the result.
   * @return the newly created instance
   */
  @ApiOperation(
      httpMethod = "POST",
      value = "Create training instance",
      notes = "This can only be done by the organizer or administrator",
      response = TrainingInstanceDTO.class,
      nickname = "createTrainingInstance",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training instance has been created.",
            response = TrainingInstanceDTO.class),
        @ApiResponse(
            code = 400,
            message = "The provided training instance is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training definition has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "The training instance start time and end time are not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTrainingInstance(
      @ApiParam(value = "Training instance to be created", required = true) @Valid @RequestBody
          TrainingInstanceCreateDTO trainingInstanceCreateDTO,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    TrainingInstanceDTO trainingInstanceResource =
        trainingInstanceFacade.create(trainingInstanceCreateDTO);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingInstanceResource));
  }

  /**
   * Update Training Instance.
   *
   * @param trainingInstanceUpdateDTO the Training Instance to be updated
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PUT",
      value = "Update training instance",
      notes = "This can only be done by organizer of training instance or administrator",
      nickname = "updateTrainingInstance",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training instance has been updated."),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message = "The training instance start time and end time are not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> updateTrainingInstance(
      @ApiParam(value = "Training instance to be updated") @RequestBody @Valid
          TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
    String newToken = trainingInstanceFacade.update(trainingInstanceUpdateDTO);
    return new ResponseEntity<>(newToken, HttpStatus.OK);
  }

  /**
   * Delete Training Instance.
   *
   * @param id id of the Training Instance to be deleted
   * @param forceDelete the force delete
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Delete training instance",
      notes = "This can only be done by organizer of training instance or administrator",
      nickname = "deleteTrainingInstance")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training instance has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training instance is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message =
                "The training instance cannot be deleted for the specific reason stated in the error message.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @DeleteMapping(path = "/{instanceId}")
  public ResponseEntity<Void> deleteTrainingInstance(
      @ApiParam(value = "Id of training instance to be deleted", required = true)
          @PathVariable("instanceId")
          Long id,
      @ApiParam(
              value =
                  "Indication if this training run must be deleted no matter of any check (force it)",
              required = false)
          @RequestParam(value = "forceDelete", required = false)
          boolean forceDelete) {
    trainingInstanceFacade.delete(id, forceDelete);
    return ResponseEntity.ok().build();
  }

  /**
   * Assign pool response entity.
   *
   * @param id the id
   * @param trainingInstanceAssignPoolIdDTO the training instance assign pool id dto
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PATCH",
      value = "Assign pool to the training instance",
      notes = "This can only be done by organizer of training instance or administrator",
      nickname = "assignPool")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The training instance has been updated."),
        @ApiResponse(
            code = 400,
            message = "The provided training instance is not valid.",
            response = ApiError.class),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 409,
            message =
                "The training instance cannot be updated for the specific reason stated in the error message.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PatchMapping(path = "/{instanceId}/assign-pool")
  public ResponseEntity<TrainingInstanceBasicInfoDTO> assignPool(
      @ApiParam(value = "Id of training instance to be updated", required = true)
          @PathVariable("instanceId")
          Long id,
      @ApiParam(value = "Id of pool to be assigned to training instance", required = true)
          @Valid
          @RequestBody
          TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
    return ResponseEntity.ok(
        trainingInstanceFacade.assignPoolToTrainingInstance(id, trainingInstanceAssignPoolIdDTO));
  }

  /**
   * Unassign pool response entity.
   *
   * @param id the id
   * @return the response entity
   */
  @ApiOperation(
      httpMethod = "PATCH",
      value = "Unassign pool of training instance",
      notes = "This can only be done by organizer of training instance or administrator",
      nickname = "unassignPool")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "The pool has been unassigned."),
        @ApiResponse(
            code = 409,
            message = "The training instance has not assigned pool.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @PatchMapping(path = "/{instanceId}/unassign-pool")
  public ResponseEntity<TrainingInstanceBasicInfoDTO> unassignPool(
      @ApiParam(value = "Id of training instance to unassign pool.", required = true)
          @PathVariable("instanceId")
          Long id) {
    return ResponseEntity.ok(trainingInstanceFacade.unassignPoolInTrainingInstance(id));
  }

  /**
   * Get all Training Runs by Training Instance id.
   *
   * @param instanceId the Training Instance id
   * @param isActive if true, only active Training Runs are returned
   * @param pageable Pageable parameter with information about pagination.
   * @param fields attributes of the object to be returned as the result.
   * @return all Training Runs in given Training Instance.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get all training runs of specific training instance",
      response = TrainingRunsRestController.TrainingRunRestResource.class,
      notes = "This can only be done by organizer of training instance or administrator",
      nickname = "findAllTrainingRunsByTrainingInstanceId",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The training runs have been found.",
            response = TrainingRunsRestController.TrainingRunRestResource.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @GetMapping(path = "/{instanceId}/training-runs", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAllTrainingRunsByTrainingInstanceId(
      @ApiParam(value = "Training Instance Id", required = true) @PathVariable("instanceId")
          Long instanceId,
      @ApiParam(value = "If only active or not active training runs should be returned.")
          @RequestParam(value = "isActive", required = false)
          Boolean isActive,
      @ApiParam(value = "Pagination support.") Pageable pageable,
      @ApiParam(value = "Fields which should be returned in REST API response", required = false)
          @RequestParam(value = "fields", required = false)
          String fields) {
    PageResultResource<TrainingRunDTO> trainingRunResource =
        trainingInstanceFacade.findTrainingRunsByTrainingInstance(instanceId, isActive, pageable);
    Squiggly.init(objectMapper, fields);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingRunResource));
  }

  /**
   * Get requested organizers of training instance.
   *
   * @param trainingInstanceId id of training instance for which to get the organizers
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role designer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get organizers of training instance.",
      response = TrainingDefinitionsRestController.UserInfoRestResource.class,
      nickname = "getOrganizersOfTrainingInstance",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The organizers have been found.",
            response = TrainingDefinitionsRestController.UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(path = "/{instanceId}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getOrganizersOfTrainingInstance(
      @ApiParam(
              value = "ID of training instance for which to retrieve the organizers.",
              required = true)
          @PathVariable("instanceId")
          Long trainingInstanceId,
      @ApiParam(value = "Given name filter.", required = true)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = true)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.") Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingInstanceFacade.getOrganizersOfTrainingInstance(
            trainingInstanceId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Get requested organizers not in given training instance.
   *
   * @param trainingInstanceId id ot the training instance
   * @param givenName the given name
   * @param familyName the family name
   * @param pageable pageable parameter with information about pagination.
   * @return List of users login and full name with role organizer.
   */
  @ApiOperation(
      httpMethod = "GET",
      value = "Get organizers not in given training instance.",
      response = TrainingDefinitionsRestController.UserInfoRestResource.class,
      nickname = "findOrganizersNotInGivenTrainingInstance",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "The organizers have been found.",
            response = TrainingDefinitionsRestController.UserInfoRestResource.class),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected condition was encountered.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @GetMapping(
      path = "{instanceId}/organizers-not-in-training-instance",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getOrganizersNotInGivenTrainingInstance(
      @ApiParam(
              value =
                  "ID of the training instance which do not contains organizers you want to retrieve.",
              required = true)
          @PathVariable("instanceId")
          Long trainingInstanceId,
      @ApiParam(value = "Given name filter.", required = false)
          @RequestParam(value = "givenName", required = false)
          String givenName,
      @ApiParam(value = "Family name filter.", required = false)
          @RequestParam(value = "familyName", required = false)
          String familyName,
      @ApiParam(value = "Pagination support.") Pageable pageable) {
    PageResultResource<UserRefDTO> designers =
        trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(
            trainingInstanceId, pageable, givenName, familyName);
    return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, designers));
  }

  /**
   * Concurrently add/remove organizers with given ids to/from the training instance.
   *
   * @param trainingInstanceId id of training instance for which to retrieve organizers
   * @param organizersAddition ids of the organizers to be added to the training instance.
   * @param organizersRemoval ids of the organizers to be removed from the training instance.
   * @return the response entity
   */
  @ApiOperation(httpMethod = "PUT", value = "Edit organizers.", nickname = "editOrganizers")
  @ApiResponses(
      value = {
        @ApiResponse(code = 204, message = "The organizers of training instance have been edited."),
        @ApiResponse(
            code = 404,
            message = "The training instance has not been found.",
            response = ApiError.class),
        @ApiResponse(
            code = 500,
            message =
                "Unexpected condition was encountered. Probably error during calling other microservice.",
            response = ApiError.class)
      })
  @ApiPageableSwagger
  @PutMapping(path = "/{instanceId}/organizers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> editOrganizers(
      @ApiParam(value = "ID of training instance to be updated.", required = true)
          @PathVariable("instanceId")
          Long trainingInstanceId,
      @ApiParam(value = "Ids of the organizers to be added to the training instance.")
          @RequestParam(value = "organizersAddition", required = false)
          Set<Long> organizersAddition,
      @ApiParam(value = "Ids of the organizers to be removed from the training instance.")
          @RequestParam(value = "organizersRemoval", required = false)
          Set<Long> organizersRemoval) {
    trainingInstanceFacade.editOrganizers(
        trainingInstanceId, organizersAddition, organizersRemoval);
    return ResponseEntity.noContent().build();
  }

  @ApiModel(
      value = "TrainingInstanceRestResource",
      description =
          "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
  private static class TrainingInstanceRestResource
      extends PageResultResource<TrainingInstanceFindAllResponseDTO> {
    @JsonProperty(required = true)
    @ApiModelProperty(value = "Retrieved Training Instances from databases.")
    private List<TrainingInstanceFindAllResponseDTO> content;

    @JsonProperty(required = true)
    @ApiModelProperty(
        value =
            "Pagination including: page number, number of elements in page, size, total elements and total pages.")
    private Pagination pagination;
  }
}
