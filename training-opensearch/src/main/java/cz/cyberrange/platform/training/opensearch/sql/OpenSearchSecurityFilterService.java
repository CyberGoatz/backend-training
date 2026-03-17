package cz.cyberrange.platform.training.opensearch.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Builds OpenSearch DSL filters for securing query results based on the caller's access rights.
 *
 * <p>The produced filter restricts results to:
 *
 * <ul>
 *   <li>Indices whose name starts with {@value #ALLOWED_INDEX_PREFIX_TRAININGS} or {@value
 *       #ALLOWED_INDEX_PREFIX_CONSOLE}.
 *   <li>Documents whose {@code training_instance_id} is in {@code allowedInstanceIds} (organizer)
 *       or whose {@code training_run_id} is in {@code allowedRunIds} (trainee).
 * </ul>
 */
@Service
public class OpenSearchSecurityFilterService {

  private static final String INSTANCE_ID_FIELD = "training_instance_id";
  private static final String RUN_ID_FIELD = "training_run_id";
  private static final String INDEX_FIELD = "_index";
  private static final String ALLOWED_INDEX_PREFIX_TRAININGS = "crczp.events.trainings";
  private static final String ALLOWED_INDEX_PREFIX_CONSOLE = "crczp.logs.console";
  private static final String SYSLOG_FIELD = "syslog";
  private static final String SANDBOX_ID_FIELD = "sandbox_id";

  private final ObjectMapper objectMapper;

  /**
   * @param objectMapper the {@link ObjectMapper} used to construct JSON filter nodes
   */
  @Autowired
  public OpenSearchSecurityFilterService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Builds a combined DSL filter for the given organizer instance IDs and trainee run IDs.
   *
   * <p>The resulting DSL filter structure is:
   *
   * <pre>{@code
   * {
   *   "bool": {
   *     "must": [
   *       { "bool": { "should": [
   *           { "prefix": { "_index": "crczp.events.trainings" } },
   *           { "prefix": { "_index": "crczp.logs.console"    } }
   *       ]}},
   *       <accessFilter>
   *     ]
   *   }
   * }
   * }</pre>
   *
   * <p>When only one of the two ID sets is non-empty, {@code <accessFilter>} is a single {@code
   * terms} query. When both are non-empty it is a {@code bool.should} of two {@code terms} queries.
   *
   * @param allowedInstanceIds instance IDs the caller has organizer access to; may be {@code null}
   *     or empty
   * @param allowedRunIds run IDs the caller has trainee access to; may be {@code null} or empty
   * @return a {@link JsonNode} representing the combined DSL filter
   */
  public JsonNode buildAccessControlFilter(
      @Nullable Collection<Long> allowedInstanceIds, @Nullable Collection<Long> allowedRunIds) {
    ArrayNode mustArray = objectMapper.createArrayNode();
    mustArray.add(buildIndexPrefixFilter());
    mustArray.add(buildAccessFilter(allowedInstanceIds, allowedRunIds));

    ObjectNode boolNode = objectMapper.createObjectNode();
    boolNode.set("must", mustArray);

    ObjectNode root = objectMapper.createObjectNode();
    root.set("bool", boolNode);
    return root;
  }

  /**
   * Builds the {@code _source} exclusion node to add to the request body, ensuring restricted
   * fields are never returned by OpenSearch.
   *
   * <p>Always excludes {@value #SYSLOG_FIELD}. Excludes {@value #SANDBOX_ID_FIELD} when {@code
   * includeSandboxId} is {@code false}.
   *
   * @param includeSandboxId {@code true} to keep the {@value #SANDBOX_ID_FIELD} field in results,
   *     {@code false} to exclude it
   * @return a {@link JsonNode} of the form {@code {"excludes": ["syslog", ...]}}
   */
  public JsonNode buildSourceExcludes(boolean includeSandboxId) {
    ArrayNode excludes = objectMapper.createArrayNode();
    excludes.add(SYSLOG_FIELD);
    if (!includeSandboxId) {
      excludes.add(SANDBOX_ID_FIELD);
    }

    ObjectNode sourceNode = objectMapper.createObjectNode();
    sourceNode.set("excludes", excludes);
    return sourceNode;
  }

  private JsonNode buildIndexPrefixFilter() {
    ArrayNode shouldArray = objectMapper.createArrayNode();
    shouldArray.add(prefixQuery(INDEX_FIELD, ALLOWED_INDEX_PREFIX_TRAININGS));
    shouldArray.add(prefixQuery(INDEX_FIELD, ALLOWED_INDEX_PREFIX_CONSOLE));

    ObjectNode boolNode = objectMapper.createObjectNode();
    boolNode.set("should", shouldArray);

    ObjectNode root = objectMapper.createObjectNode();
    root.set("bool", boolNode);
    return root;
  }

  private JsonNode buildAccessFilter(
      Collection<Long> allowedInstanceIds, Collection<Long> allowedRunIds) {
    boolean hasInstances = allowedInstanceIds != null && !allowedInstanceIds.isEmpty();
    boolean hasRuns = allowedRunIds != null && !allowedRunIds.isEmpty();

    if (hasInstances && !hasRuns) {
      return termsQuery(INSTANCE_ID_FIELD, allowedInstanceIds);
    }
    if (!hasInstances && hasRuns) {
      return termsQuery(RUN_ID_FIELD, allowedRunIds);
    }

    ArrayNode shouldArray = objectMapper.createArrayNode();
    shouldArray.add(termsQuery(INSTANCE_ID_FIELD, allowedInstanceIds));
    shouldArray.add(termsQuery(RUN_ID_FIELD, allowedRunIds));

    ObjectNode boolNode = objectMapper.createObjectNode();
    boolNode.set("should", shouldArray);

    ObjectNode root = objectMapper.createObjectNode();
    root.set("bool", boolNode);
    return root;
  }

  private JsonNode termsQuery(String field, Iterable<Long> values) {
    ArrayNode valuesArray = objectMapper.createArrayNode();
    values.forEach(valuesArray::add);

    ObjectNode fieldNode = objectMapper.createObjectNode();
    fieldNode.set(field, valuesArray);

    ObjectNode termsNode = objectMapper.createObjectNode();
    termsNode.set("terms", fieldNode);
    return termsNode;
  }

  private JsonNode prefixQuery(String field, String prefix) {
    ObjectNode fieldNode = objectMapper.createObjectNode();
    fieldNode.put(field, prefix);

    ObjectNode prefixNode = objectMapper.createObjectNode();
    prefixNode.set("prefix", fieldNode);
    return prefixNode;
  }
}
