package cz.cyberrange.platform.training.opensearch.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Configuration class joining all OpenSearch related configurations and component scans. */
@Configuration
@Import({ObjectMapperConfigOpenSearch.class, RestClientConfigOpenSearch.class})
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.opensearch.logging",
        "cz.cyberrange.platform.training.opensearch.sql"
    })
public class OpenSearchServiceConfig {}
