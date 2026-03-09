package cz.cyberrange.platform.training.rest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.OpenSearchFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Elastic search rest controller.
 * Responsible for handling requests for executing user queries on OpenSearch.
 */
@Api(
    value = "/opensearch",
    tags = "OpenSearch",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    authorizations = @Authorization("bearerAuth"))
@ApiResponses(
    @ApiResponse(
        code = 401,
        message = "Full authentication is required to access this resource.",
        response = ApiError.class))
@RestController
@RequestMapping(path = "/opensearch", produces = MediaType.APPLICATION_JSON_VALUE)
public class OpenSearchRestController {

  private final OpenSearchFacade openSearchFacade;

  @Autowired
  public OpenSearchRestController(OpenSearchFacade openSearchFacade) {
    this.openSearchFacade = openSearchFacade;
  }

  @ApiOperation(
      httpMethod = "POST",
      value = "Execute SQL query on OpenSearch and return the result.",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = JsonNode.class,
      nickname = "sqlQuery")
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "SQL query executed successfully.",
        response = JsonNode.class),
    @ApiResponse(
        code = 500,
        message = "Internal server error. An error occurred while processing the SQL query.",
        response = ApiError.class)
  })
  @GetMapping(value = "/sql", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonNode> sqlQuery(
      @ApiParam(name = "query", value = "SQL query to execute.", required = true) String query)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    return ResponseEntity.ok(openSearchFacade.handleSqlQuery(query));
  }
}
