package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The type CyberGoatz api service.
 */
@Service
public class CyberGoatzApiService {

    private static final Logger LOG = LoggerFactory.getLogger(CyberGoatzApiService.class);

    private final WebClient cyberGoatzServiceWebClient;
    private final String cyberGoatzServiceURI;

    /**
     * Instantiates a new CyberGoatzApiService service.
     *
     * @param cyberGoatzServiceWebClient the web client
     * @param cyberGoatzServiceURI       CyberGoatz service URI
     */
    public CyberGoatzApiService(@Qualifier("cyberGoatzServiceWebClient") WebClient cyberGoatzServiceWebClient,
                                @Value("${cybergoatz-service.uri:}") String cyberGoatzServiceURI) {
        this.cyberGoatzServiceWebClient = cyberGoatzServiceWebClient;
        this.cyberGoatzServiceURI = cyberGoatzServiceURI;
    }

    /**
     * Deletes CyberGoatz track items referencing a training instance.
     *
     * @param trainingInstanceId id of the deleted training instance
     * @throws MicroserviceApiException error with specific message when calling CyberGoatz microservice.
     */
    public void deleteTrackItemsByTrainingInstanceId(Long trainingInstanceId) {
        if (cyberGoatzServiceURI == null || cyberGoatzServiceURI.isBlank()) {
            throw new MicroserviceApiException(
                    "CyberGoatz service URI is not configured. Cannot delete track items for training instance (ID: "
                            + trainingInstanceId + ").",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    JavaApiError.of("Missing cybergoatz-service.uri configuration."));
        }
        try {
            cyberGoatzServiceWebClient
                    .delete()
                    .uri("/admin/tracks/training-instances/{trainingInstanceId}/items", trainingInstanceId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            LOG.info("Deleted CyberGoatz track items for training instance {}.", trainingInstanceId);
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling CyberGoatz API to delete track items for particular training instance (ID: "
                    + trainingInstanceId + ").", ex);
        }
    }
}
