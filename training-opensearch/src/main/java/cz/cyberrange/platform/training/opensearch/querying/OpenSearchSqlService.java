package cz.cyberrange.platform.training.opensearch.querying;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Service for executing SQL queries against OpenSearch. */
@Service
public class OpenSearchSqlService {

  private static final Logger logger = LoggerFactory.getLogger(OpenSearchSqlService.class);

  private static final String INSTANCE_INDEX_FIELD = "training_instance_id";
  private static final String RUN_INDEX_FIELD = "training_run_id";
  private static final String SQL_ENDPOINT = "/_sql?format=json";
  private static final String CURSOR_FIELD = "cursor";
  private static final String FETCH_SIZE_FIELD = "fetch_size";

  private final RestClient restClient;
  private final ObjectMapper jacksonObjectMapper;
  private final int maxResultCount;

  /**
   * @param restClient the OpenSearch low-level {@link RestClient}
   * @param jacksonObjectMapper the Jackson {@link ObjectMapper}
   * @param maxResultCount the maximum number of rows a single query may return in a single response
   */
  @Autowired
  public OpenSearchSqlService(
      @Qualifier("openSearchClient") RestClient restClient,
      ObjectMapper jacksonObjectMapper,
      @Value("${opensearch.max-result-count}") int maxResultCount) {
    this.restClient = restClient;
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
   * The resulting filter is: {@code training_instance_id IN (allowedInstanceIds) OR training_run_id
   * IN (allowedRunIds)}
   *
   * <p>If both lists are empty, an empty {@link OpenSearchQueryResult} is returned (deny all).
   *
   * @param sqlQuery the OpenSearch SQL compatible query to execute
   * @param allowedInstanceIds list of instance IDs the user has access to (organizer role)
   * @param allowedRunIds list of run IDs the user has access to (trainee role)
   * @return an {@link OpenSearchQueryResult} containing the page data and pagination metadata, or a
   *     result wrapping an empty object if both allowedInstanceIds and allowedRunIds are empty
   * @throws OpenSearchQueryException if an error occurs while executing the query or processing the
   *     response
   * @throws OpenSearchSerializeException if an error occurs while parsing the response JSON
   */
  public OpenSearchQueryResult executeSqlQueryWithAccessControl(
      String sqlQuery,
      @Nullable Collection<Long> allowedInstanceIds,
      @Nullable Collection<Long> allowedRunIds)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    boolean hasNoInstancesAvailable = allowedInstanceIds == null || allowedInstanceIds.isEmpty();
    boolean hasNoRunsAvailable = allowedRunIds == null || allowedRunIds.isEmpty();
    if (hasNoInstancesAvailable && hasNoRunsAvailable) {
      // No access to any instance or run, return empty result without querying OpenSearch
      return new OpenSearchQueryResult(jacksonObjectMapper.createObjectNode(), false);
    }
    return this.executeSqlQuery(sqlQuery, this.buildSafeFilter(allowedInstanceIds, allowedRunIds));
  }

  /**
   * Executes a SQL query without any additional filtering.
   *
   * <p>Please ensure the caller is authorized as admin.
   *
   * @param sqlQuery the OpenSearch SQL compatible query to execute
   * @return an {@link OpenSearchQueryResult} containing the page data and pagination metadata
   * @throws OpenSearchQueryException if an error occurs while executing the query or processing the
   *     response
   * @throws OpenSearchSerializeException if an error occurs while parsing the response JSON
   */
  public OpenSearchQueryResult executeSqlQueryFromAdmin(String sqlQuery)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    return this.executeSqlQuery(sqlQuery, null);
  }

  private OpenSearchQueryResult executeSqlQuery(String sqlQuery, @Nullable JsonNode filter)
      throws OpenSearchQueryException, OpenSearchSerializeException {
    String requestBody = buildRequestBody(sqlQuery, filter);
    Request request = new Request("POST", SQL_ENDPOINT);
    request.setJsonEntity(requestBody);

    try {
      Response response = restClient.performRequest(request);
      String responseBody = EntityUtils.toString(response.getEntity());
      JsonNode result = jacksonObjectMapper.readTree(responseBody);
      boolean hasMore = result.has(CURSOR_FIELD);
      return new OpenSearchQueryResult(result, hasMore);
    } catch (JsonProcessingException e) {
      logger.error("Failed to parse OpenSearch SQL response JSON", e);
      throw new OpenSearchSerializeException("Failed to parse OpenSearch SQL response JSON", e);
    } catch (IOException e) {
      logger.error(
          "Failed to execute SQL query. Query: {}, access filter: {}", sqlQuery, filter, e);
      throw new OpenSearchQueryException("Failed to execute SQL query", e);
    }
  }

  /**
   * Builds the JSON request body for the SQL query, including the optional filter.
   *
   * <p>Sets {@code fetch_size} to {@code maxResultCount} so that a {@code cursor} field in the
   * response unambiguously signals that more rows exist beyond the current page.
   *
   * @param sqlQuery the SQL query to execute
   * @param filter an optional {@link JsonNode} representing the filter to apply
   * @return a JSON string representing the request body
   */
  private String buildRequestBody(String sqlQuery, @Nullable JsonNode filter) {
    ObjectNode requestBody = jacksonObjectMapper.createObjectNode();
    requestBody.put("query", sqlQuery);
    requestBody.put(FETCH_SIZE_FIELD, maxResultCount);
    if (filter != null) {
      requestBody.set("filter", filter);
    }
    return requestBody.toString();
  }

  /**
   * Builds a combined filter covering all allowed instances (organizer) and run IDs (trainee). The
   * resulting filter is a boolean OR of the two sets of IDs.
   *
   * @param allowedInstanceIds list of instance IDs the user has access to (organizer role)
   * @param allowedRunIds list of run IDs the user has access to (trainee role)
   * @return a {@link JsonNode} representing the combined filter
   * @throws IllegalArgumentException if both allowedInstanceIds and allowedRunIds are null or empty
   */
  private JsonNode buildSafeFilter(
      Collection<Long> allowedInstanceIds, Collection<Long> allowedRunIds) {
    boolean hasInstances = allowedInstanceIds != null && !allowedInstanceIds.isEmpty();
    boolean hasRuns = allowedRunIds != null && !allowedRunIds.isEmpty();

    if (!hasInstances && !hasRuns) {
      throw new IllegalArgumentException(
          "At least one of allowedInstanceIds or allowedRunIds must be non-empty");
    }

    if (hasInstances && !hasRuns) {
      return this.termsQuery(INSTANCE_INDEX_FIELD, allowedInstanceIds);
    }

    if (!hasInstances) {
      return this.termsQuery(RUN_INDEX_FIELD, allowedRunIds);
    }

    ArrayNode shouldArray = jacksonObjectMapper.createArrayNode();
    shouldArray.add(this.termsQuery(INSTANCE_INDEX_FIELD, allowedInstanceIds));
    shouldArray.add(this.termsQuery(RUN_INDEX_FIELD, allowedRunIds));

    ObjectNode boolNode = jacksonObjectMapper.createObjectNode();
    boolNode.set("should", shouldArray);

    ObjectNode root = jacksonObjectMapper.createObjectNode();
    root.set("bool", boolNode);
    return root;
  }

  /**
   * Builds a {@code terms} filter as a {@link JsonNode} matching any of the given numeric values
   * against a field.
   *
   * @param field the document field name to filter on
   * @param values the collection of {@link Long} values to match
   * @return a {@link JsonNode} of the form {@code {"terms": {"<field>": [v1, v2, ...]}}}
   */
  private JsonNode termsQuery(String field, Iterable<Long> values) {
    ArrayNode valuesArray = jacksonObjectMapper.createArrayNode();
    values.forEach(valuesArray::add);

    ObjectNode fieldNode = jacksonObjectMapper.createObjectNode();
    fieldNode.set(field, valuesArray);

    ObjectNode termsNode = jacksonObjectMapper.createObjectNode();
    termsNode.set("terms", fieldNode);
    return termsNode;
  }
}
