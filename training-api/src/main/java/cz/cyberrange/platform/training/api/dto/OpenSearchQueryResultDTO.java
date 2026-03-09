package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Wraps the result of an OpenSearch SQL query with pagination metadata.
 *
 * <p>When {@link #isHasMore()} returns {@code true} the caller should repeat the request with an
 * incremented page index to retrieve the next chunk of rows.
 */
@Getter
@EqualsAndHashCode
@Setter
@NoArgsConstructor
@ToString
@ApiModel(
    value = "OpenSearchQueryResultDTO",
    description =
        "Result of an OpenSearch SQL query execution, including the raw response data and"
            + " pagination metadata.")
public class OpenSearchQueryResultDTO {

  @ApiModelProperty(
      value =
          "Raw OpenSearch SQL response for the current page, containing the schema and data rows.",
      required = true)
  private JsonNode data;

  @ApiModelProperty(
      value =
          "Indicates whether more pages of results are available. When true, the HTTP response"
              + " status is 206 Partial Content. Use SQL LIMIT / OFFSET in your query to retrieve"
              + " further pages.",
      required = true,
      example = "false")
  private boolean hasMore;

  @ApiModelProperty(
      value = "Maximum number of rows returned per page, as configured on the server.",
      required = true,
      example = "10000")
  private int pageSize;

  public OpenSearchQueryResultDTO(JsonNode data, boolean hasMore, int pageSize) {
    this.data = data;
    this.hasMore = hasMore;
    this.pageSize = pageSize;
  }
}
