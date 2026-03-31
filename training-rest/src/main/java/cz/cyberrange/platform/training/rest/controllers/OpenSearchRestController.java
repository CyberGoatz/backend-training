package cz.cyberrange.platform.training.rest.controllers;

import cz.cyberrange.platform.training.api.dto.OpenSearchQueryResultDTO;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.service.facade.OpenSearchFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * openSearch search rest controller. Responsible for handling requests for executing user queries
 * on OpenSearch.
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
      httpMethod = "GET",
      value = "Execute SQL query on OpenSearch and return the result.",
      notes =
          "Returns up to pageSize rows. When more data is available, hasMore is true and"
              + " the response status is 206 Partial Content. Use SQL LIMIT / OFFSET in your"
              + " query to paginate further.",
      produces = MediaType.APPLICATION_JSON_VALUE,
      response = OpenSearchQueryResultDTO.class,
      nickname = "sqlQuery")
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "SQL query executed successfully, all results fit within one page.",
        response = OpenSearchQueryResultDTO.class),
    @ApiResponse(
        code = 206,
        message = "SQL query executed successfully, but more pages are available.",
        response = OpenSearchQueryResultDTO.class),
    @ApiResponse(
        code = 403,
        message = "User is neither not an admin, organiser nor a trainee",
        response = ApiError.class),
    @ApiResponse(
        code = 500,
        message = "Internal server error. An error occurred while processing the SQL query.",
        response = ApiError.class)
  })
  @GetMapping(value = "/sql", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OpenSearchQueryResultDTO> sqlQuery(
      @ApiParam(name = "query", value = "SQL query to execute.", required = true) String query)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    OpenSearchQueryResultDTO result = openSearchFacade.handleSqlQuery(query);
    HttpStatus status = result.isHasMore() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
    return ResponseEntity.status(status).body(result);
  }
}
