package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.OpenSearchQueryResultDTO;
import cz.cyberrange.platform.training.opensearch.sql.OpenSearchSqlResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Value;

/**
 * MapStruct mapper for converting {@link OpenSearchSqlResult} to {@link
 * OpenSearchQueryResultDTO}.
 *
 * <p>The {@code pageSize} field is not present on {@link OpenSearchSqlResult} — it is injected
 * from the {@code opensearch.max-result-count} property and stamped onto the DTO during mapping.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class OpenSearchQueryResultMapper {

  @Value("${opensearch.max-result-count}")
  protected int maxResultCount;

  /**
   * Maps an {@link OpenSearchSqlResult} to an {@link OpenSearchQueryResultDTO}, automatically
   * populating {@code pageSize} from the configured {@code opensearch.max-result-count} property.
   *
   * @param result the {@link OpenSearchSqlResult} to map
   * @return the corresponding {@link OpenSearchQueryResultDTO}
   */
  @Mapping(target = "pageSize", expression = "java(maxResultCount)")
  public abstract OpenSearchQueryResultDTO mapToDTO(OpenSearchSqlResult result);
}
