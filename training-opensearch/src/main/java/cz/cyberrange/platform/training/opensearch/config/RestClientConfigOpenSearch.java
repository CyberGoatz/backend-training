package cz.cyberrange.platform.training.opensearch.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that creates an OpenSearch low-level {@link RestClient} bean used for
 * executing raw HTTP requests against OpenSearch.
 */
@Configuration
public class RestClientConfigOpenSearch {

    @Value("${opensearch.protocol}")
    private String protocol;

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    /**
     * Creates and configures the OpenSearch low-level {@link RestClient}.
     *
     * @return a configured {@link RestClient} instance
     */
    @Bean("openSearchClient")
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, protocol)).build();
    }
}
