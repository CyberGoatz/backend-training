package cz.cyberrange.platform.training.opensearch.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import org.springframework.lang.Nullable;

/**
 * Builder for constructing the full JSON request body for an OpenSearch SQL query.
 *
 * <p>Security layers are applied incrementally, corresponding to the levels defined by {@link
 * QuerySecurityLevel}. Each layer method further restricts the query results. Call {@link #build()}
 * to obtain the final {@link ObjectNode} ready to be sent as the HTTP request body.
 *
 * <p>Example usage for {@link QuerySecurityLevel#RESTRICT_INDEXES_REMOVE_SYSTEM_INFO}:
 *
 * <pre>{@code
 * ObjectNode body = new OpenSearchSqlRequestBodyBuilder(objectMapper, securityFilterService, sql, fetchSize)
 *     .restrictIndexes(instanceIds, runIds)
 *     .removeSystemInfo()
 *     .build();
 * }</pre>
 */
public class OpenSearchSqlRequestBodyBuilder {

  private static final String QUERY_FIELD = "query";
  private static final String FETCH_SIZE_FIELD = "fetch_size";
  private static final String FILTER_FIELD = "filter";
  private static final String SOURCE_FIELD = "_source";
  private static final String EXCLUDES_FIELD = "excludes";
  private static final String SYSLOG_FIELD = "syslog";
  private static final String ANSWERS_FIELD = "answers";
  private static final String PASSKEY_CONTENT_FIELD = "passkey_content";
  private static final String ANSWER_CONTENT_FIELD = "answer_content";

  private final ObjectMapper objectMapper;
  private final OpenSearchSecurityFilterService securityFilterService;
  private final String sqlQuery;
  private final int fetchSize;

  private com.fasterxml.jackson.databind.JsonNode filter;
  private final ArrayNode excludedFields;

  /**
   * @param objectMapper the {@link ObjectMapper} used to construct JSON nodes
   * @param securityFilterService the {@link OpenSearchSecurityFilterService} used to build DSL
   *     access control filters
   * @param sqlQuery the OpenSearch SQL-compatible query string
   * @param fetchSize the maximum number of rows to return per page
   */
  public OpenSearchSqlRequestBodyBuilder(
      ObjectMapper objectMapper,
      OpenSearchSecurityFilterService securityFilterService,
      String sqlQuery,
      int fetchSize) {
    this.objectMapper = objectMapper;
    this.securityFilterService = securityFilterService;
    this.sqlQuery = sqlQuery;
    this.fetchSize = fetchSize;
    this.excludedFields = objectMapper.createArrayNode();
  }

  /**
   * Restricts results to the allowed training and console indices and enforces per-instance and
   * per-run access control based on the provided ID collections.
   *
   * <p>Corresponds to {@link QuerySecurityLevel#RESTRICT_INDEXES}.
   *
   * @param allowedInstanceIds instance IDs the caller has organizer access to; may be {@code null}
   *     or empty
   * @param allowedRunIds run IDs the caller has trainee access to; may be {@code null} or empty
   * @return this builder
   */
  public OpenSearchSqlRequestBodyBuilder restrictIndexes(
      @Nullable Collection<Long> allowedInstanceIds, @Nullable Collection<Long> allowedRunIds) {
    this.filter = securityFilterService.buildAccessControlFilter(allowedInstanceIds, allowedRunIds);
    return this;
  }

  /**
   * Excludes the {@code syslog} field from every returned document's {@code _source}, removing
   * system-level information from results.
   *
   * <p>Corresponds to {@link QuerySecurityLevel#RESTRICT_INDEXES_REMOVE_SYSTEM_INFO}.
   *
   * @return this builder
   */
  public OpenSearchSqlRequestBodyBuilder removeSystemInfo() {
    excludedFields.add(SYSLOG_FIELD);
    return this;
  }

  /**
   * Excludes the {@code answers}, {@code passkey_content}, and {@code answer_content} fields from
   * every returned document's {@code _source}, removing trainee-sensitive data from results.
   *
   * <p>Corresponds to {@link
   * QuerySecurityLevel#RESTRICT_INDEXES_REMOVE_SYSTEM_INFO_REMOVE_TRAINEE_SENSITIVE_DATA}.
   *
   * @return this builder
   */
  public OpenSearchSqlRequestBodyBuilder removeAnswers() {
    excludedFields.add(ANSWERS_FIELD);
    excludedFields.add(PASSKEY_CONTENT_FIELD);
    excludedFields.add(ANSWER_CONTENT_FIELD);
    return this;
  }

  /**
   * Builds the complete OpenSearch SQL request body from the accumulated query and security layers.
   *
   * @return an {@link ObjectNode} containing the full request body
   */
  public ObjectNode build() {
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put(QUERY_FIELD, sqlQuery);
    requestBody.put(FETCH_SIZE_FIELD, fetchSize);
    if (filter != null) {
      requestBody.set(FILTER_FIELD, filter);
    }
    if (!excludedFields.isEmpty()) {
      ObjectNode sourceNode = objectMapper.createObjectNode();
      sourceNode.set(EXCLUDES_FIELD, excludedFields);
      requestBody.set(SOURCE_FIELD, sourceNode);
    }
    return requestBody;
  }
}
