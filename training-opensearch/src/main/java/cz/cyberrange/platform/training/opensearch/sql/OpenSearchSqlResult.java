package cz.cyberrange.platform.training.opensearch.sql;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

/**
 * Wraps the result of an OpenSearch SQL query with pagination metadata.
 *
 * <p>When {@link #isHasMore()} returns {@code true} the caller should repeat the request with an
 * incremented page index to retrieve the next chunk of rows.
 */
@Getter
public class OpenSearchSqlResult {

  /**
   * -- GETTER -- Returns the query result data for the current page.
   *
   * @return a {@link JsonNode} containing the OpenSearch SQL response
   */
  private final JsonNode data;

  /**
   * -- GETTER -- Returns whether additional pages are available.
   *
   * @return {@code true} if there is at least one more page of results
   */
  private final boolean hasMore;

  /**
   * @param data the {@link JsonNode} returned by OpenSearch for the current page
   * @param hasMore {@code true} if at least one additional page of results exists
   */
  public OpenSearchSqlResult(JsonNode data, boolean hasMore) {
    this.data = data;
    this.hasMore = hasMore;
  }
}
