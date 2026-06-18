package cz.cyberrange.platform.training.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * The type Web client config.
 */
@Import(ObjectMappersConfiguration.class)
@Configuration
public class WebClientConfig {


    @Value("${openstack-server.uri}")
    private String openStackURI;
    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;
    @Value("${elasticsearch-service.uri}")
    private String elasticsearchServiceURI;
    @Value("${answers-storage.uri}")
    private String answersStorageURI;
    @Value("${training-feedback-service.uri}")
    private String trainingFeedbackServiceURI;
    @Value("${cybergoatz-service.uri:}")
    private String cyberGoatzServiceURI;
    @Value("${training.sandbox.service-account.token-uri:}")
    private String sandboxServiceAccountTokenUri;
    @Value("${training.sandbox.service-account.client-id:}")
    private String sandboxServiceAccountClientId;
    @Value("${training.sandbox.service-account.client-secret:}")
    private String sandboxServiceAccountClientSecret;

    private ObjectMapper objectMapper;
    private String cachedSandboxServiceAccountToken;
    private Instant cachedSandboxServiceAccountTokenExpiresAt = Instant.EPOCH;

    @Autowired
    public WebClientConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Openstack service web client web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("sandboxServiceWebClient")
    public WebClient sandboxServiceWebClient() {
        return WebClient.builder()
                .baseUrl(openStackURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(true));
                    exchangeFilterFunctions.add(openStackSandboxServiceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * User management service web client web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("userManagementServiceWebClient")
    public WebClient userManagementServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userAndGroupURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(false));
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * Elasticsearch service web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("elasticsearchServiceWebClient")
    public WebClient elasticsearchServiceWebClient() {
        return WebClient.builder()
                .baseUrl(elasticsearchServiceURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(false));
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    /**
     * Answers storage web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("answersStorageWebClient")
    public WebClient answersStorageWebClient() {
        return WebClient.builder()
                .baseUrl(answersStorageURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(false));
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * Training feedback service web client.
     *
     * @return the web client
     */
    @Bean
    public WebClient trainingFeedbackServiceWebClient() {
        return WebClient.builder()
                .baseUrl(trainingFeedbackServiceURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(false));
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * CyberGoatz service web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("cyberGoatzServiceWebClient")
    public WebClient cyberGoatzServiceWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader(false));
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                });
        if (!cyberGoatzServiceURI.isBlank()) {
            builder.baseUrl(cyberGoatzServiceURI);
        }
        return builder.build();
    }

    private ExchangeFilterFunction addSecurityHeader(boolean useSandboxServiceAccountFallback) {
        return (request, next) -> {
            String token = null;
            if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwtAuthentication) {
                Jwt jwtToken = jwtAuthentication.getToken();
                token = jwtToken.getTokenValue();
            } else if (useSandboxServiceAccountFallback) {
                token = getSandboxServiceAccountToken();
            }

            if (token == null || token.isBlank()) {
                return next.exchange(request);
            }
            ClientRequest filtered = ClientRequest.from(request)
                    .header("Authorization", "Bearer " + token)
                    .build();
            return next.exchange(filtered);
        };
    }

    private synchronized String getSandboxServiceAccountToken() {
        if (cachedSandboxServiceAccountToken != null
                && cachedSandboxServiceAccountTokenExpiresAt.isAfter(Instant.now().plusSeconds(30))) {
            return cachedSandboxServiceAccountToken;
        }
        if (sandboxServiceAccountTokenUri.isBlank()
                || sandboxServiceAccountClientId.isBlank()
                || sandboxServiceAccountClientSecret.isBlank()) {
            throw new IllegalStateException("Sandbox service-account OAuth2 client is not configured.");
        }

        Map<?, ?> response = WebClient.builder()
                .build()
                .post()
                .uri(sandboxServiceAccountTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", sandboxServiceAccountClientId)
                        .with("client_secret", sandboxServiceAccountClientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Sandbox service-account OAuth2 token response did not contain access_token.");
        }
        long expiresIn = response.get("expires_in") instanceof Number number ? number.longValue() : 60L;
        cachedSandboxServiceAccountToken = response.get("access_token").toString();
        cachedSandboxServiceAccountTokenExpiresAt = Instant.now().plusSeconds(Math.max(expiresIn - 30L, 1L));
        return cachedSandboxServiceAccountToken;
    }

    private ExchangeFilterFunction openStackSandboxServiceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        PythonApiError pythonApiError = obtainSuitablePythonApiError(errorBody);
                        throw new CustomWebClientException(clientResponse.statusCode(), pythonApiError);

                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    private PythonApiError obtainSuitablePythonApiError(String errorBody) {
        if (errorBody == null || errorBody.isBlank()) {
            return PythonApiError.of("No specific detail provided.");
        }
        try {
            return objectMapper.readValue(errorBody, PythonApiError.class);
        } catch (IOException e) {
            return PythonApiError.of("Could not obtain error detail. Error body is: " + errorBody);
        }
    }

    private ExchangeFilterFunction javaMicroserviceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        JavaApiError javaApiError = obtainSuitableJavaApiError(errorBody);
                        throw new CustomWebClientException(clientResponse.statusCode(), javaApiError);
                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    private JavaApiError obtainSuitableJavaApiError(String errorBody) {
        if (errorBody == null || errorBody.isBlank()) {
            return JavaApiError.of("No specific message provided.");
        }
        try {
            return objectMapper.readValue(errorBody, JavaApiError.class);
        } catch (IOException e) {
            return JavaApiError.of("Could not obtain error message. Error body is: " + errorBody);
        }
    }
}
