package cz.cyberrange.platform.training.opensearch.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchQueryException;
import cz.cyberrange.platform.training.opensearch.logging.exceptions.OpenSearchSerializeException;
import java.io.IOException;
import java.util.Collection;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Service for executing SQL queries against OpenSearch. */
@Service
public class OpenSearchSqlService {

  private static final Logger logger = LoggerFactory.getLogger(OpenSearchSqlService.class);

  private static final String SQL_ENDPOINT = "/_plugins/_sql?format=json";
  private static final String CURSOR_FIELD = "cursor";

  private final RestClient restClient;
  private final ObjectMapper jacksonObjectMapper;
  private final int maxResultCount;
  private final OpenSearchSecurityFilterService securityFilterService;

  /**
   * @param restClient the OpenSearch low-level {@link RestClient}
   * @param jacksonObjectMapper the Jackson {@link ObjectMapper}
   * @param maxResultCount the maximum number of rows a single query may return in a single response
   */
  @Autowired
  public OpenSearchSqlService(
      @Qualifier("openSearchClient") RestClient restClient,
      ObjectMapper jacksonObjectMapper,
      OpenSearchSecurityFilterService securityFilterService,
      @Value("${opensearch.max-result-count}") int maxResultCount) {
    this.restClient = restClient;
    this.securityFilterService = securityFilterService;
    this.jacksonObjectMapper = jacksonObjectMapper;
    this.maxResultCount = maxResultCount;
  }

  /**
   * Executes a SQL query restricted to the given allowed instance and run IDs.
   *
   * <p>The filter logic is:
   *
   * <ul>
   *   <li>Access to a whole instance (organizer) is expressed via {@code allowedInstanceIds}.
   *   <li>Access to a specific run only (trainee) is expressed via {@code allowedRunIds}.
   *   <li>When a user is both an organizer of an instance and a trainee of a run within that same
   *       instance, the instance ID covers all runs — the run ID is redundant but harmless.
   * </ul>
   *
   * <p>If both ID lists are empty and the security level requires access control, an empty {@link
   * OpenSearchSqlResult} is returned (deny all) without querying OpenSearch.
   *
   * @param sqlQuery the OpenSearch SQL compatible query to execute
   * @param securityLevel the {@link QuerySecurityLevel} determining which security layers to apply
   * @param allowedInstanceIds list of instance IDs the user has access to (organizer role)
   * @param allowedRunIds list of run IDs the user has access to (trainee role)
   * @return an {@link OpenSearchSqlResult} containing the page data and pagination metadata, or a
   *     result wrapping an empty object if access is denied
   * @throws OpenSearchQueryException if an error occurs while executing the query or processing the
   *     response
   * @throws OpenSearchSerializeException if an error occurs while parsing the response JSON
   */
  public OpenSearchSqlResult executeSqlQuery(
      @NonNull String sqlQuery,
      @NonNull QuerySecurityLevel securityLevel,
      @Nullable Collection<Long> allowedInstanceIds,
      @Nullable Collection<Long> allowedRunIds)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    boolean hasNoAccess =
        (allowedInstanceIds == null || allowedInstanceIds.isEmpty())
            && (allowedRunIds == null || allowedRunIds.isEmpty());
    if (securityLevel != QuerySecurityLevel.SHOW_ALL && hasNoAccess) {
      return new OpenSearchSqlResult(jacksonObjectMapper.createObjectNode(), false);
    }
    ObjectNode requestBody =
        buildRequestBody(sqlQuery, securityLevel, allowedInstanceIds, allowedRunIds);
    return this.buildAndExecuteQuery(requestBody);
  }

  /**
   * Executes a SQL query without any access-control filtering.
   *
   * <p>Please ensure the caller is authorized as admin.
   *
   * @param sqlQuery the OpenSearch SQL compatible query to execute
   * @param securityLevel the {@link QuerySecurityLevel} determining which security layers to apply
   * @return an {@link OpenSearchSqlResult} containing the page data and pagination metadata
   * @throws OpenSearchQueryException if an error occurs while executing the query or processing the
   *     response
   * @throws OpenSearchSerializeException if an error occurs while parsing the response JSON
   */
  public OpenSearchSqlResult executeSqlQuery(
      String sqlQuery, @NonNull QuerySecurityLevel securityLevel)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    return this.executeSqlQuery(sqlQuery, securityLevel, null, null);
  }

  private OpenSearchSqlResult buildAndExecuteQuery(ObjectNode requestBody)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    Request request = new Request("POST", SQL_ENDPOINT);
    request.setJsonEntity(requestBody.toString());

    logger.info("Executing sql query with request body: {}", requestBody);

    try {
      Response response = restClient.performRequest(request);
      String responseBody = EntityUtils.toString(response.getEntity());
      JsonNode result = jacksonObjectMapper.readTree(responseBody);
      boolean hasMore = result.has(CURSOR_FIELD);
      return new OpenSearchSqlResult(result, hasMore);
    } catch (JsonProcessingException e) {
      logger.error("Failed to parse OpenSearch SQL response JSON", e);
      throw new OpenSearchSerializeException("Failed to parse OpenSearch SQL response JSON", e);
    } catch (IOException e) {
      logger.error("Failed to execute SQL query. Request body: {}", requestBody, e);
      throw new OpenSearchQueryException("Failed to execute SQL query", e);
    }
  }

  /**
   * Builds the full JSON request body for the given SQL query and security level using {@link
   * OpenSearchSqlRequestBodyBuilder}.
   *
   * @param sqlQuery the SQL query to execute
   * @param securityLevel the {@link QuerySecurityLevel} determining which layers to apply
   * @param allowedInstanceIds instance IDs for access control; may be {@code null} or empty
   * @param allowedRunIds run IDs for access control; may be {@code null} or empty
   * @return the complete request body as an {@link ObjectNode}
   * @throws UnsupportedOperationException if {@code securityLevel} has no implementation yet
   */
  private ObjectNode buildRequestBody(
      String sqlQuery,
      QuerySecurityLevel securityLevel,
      @Nullable Collection<Long> allowedInstanceIds,
      @Nullable Collection<Long> allowedRunIds) {
    OpenSearchSqlRequestBodyBuilder builder =
        new OpenSearchSqlRequestBodyBuilder(
            jacksonObjectMapper, securityFilterService, sqlQuery, maxResultCount);
    return switch (securityLevel) {
      case SHOW_ALL -> builder.build();
      case RESTRICT_INDEXES_REMOVE_SYSTEM_INFO_REMOVE_TRAINEE_SENSITIVE_DATA ->
          builder
              .restrictIndexes(allowedInstanceIds, allowedRunIds)
              .removeSystemInfo()
              .removeAnswers()
              .build();
      case RESTRICT_INDEXES_REMOVE_SYSTEM_INFO ->
          builder.restrictIndexes(allowedInstanceIds, allowedRunIds).removeSystemInfo().build();
      case RESTRICT_INDEXES -> builder.restrictIndexes(allowedInstanceIds, allowedRunIds).build();
    };
  }
}
