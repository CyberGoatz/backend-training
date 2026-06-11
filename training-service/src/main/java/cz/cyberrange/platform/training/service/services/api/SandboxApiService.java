package cz.cyberrange.platform.training.service.services.api;

import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.LockedPoolInfo;
import cz.cyberrange.platform.training.api.responses.PoolInfoDTO;
import cz.cyberrange.platform.training.api.responses.SandboxDefinitionInfo;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.api.responses.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * The type Sandbox Api service.
 */
@Service
public class SandboxApiService {

    private static final Logger LOG = LoggerFactory.getLogger(SandboxApiService.class);
    private WebClient sandboxServiceWebClient;

    /**
     * Instantiates a new SandboxApiService service.
     *
     * @param sandboxServiceWebClient the web client
     */
    public SandboxApiService(@Qualifier("sandboxServiceWebClient") WebClient sandboxServiceWebClient) {
        this.sandboxServiceWebClient = sandboxServiceWebClient;
    }

    /**
     * Gets sandbox definition id.
     *
     * @param poolId the pool id
     * @return the sandbox definition id
     */
    public SandboxDefinitionInfo getSandboxDefinitionId(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/definition", poolId)
                    .retrieve()
                    .bodyToMono(SandboxDefinitionInfo.class)
                    .block();
        } catch (CustomWebClientException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ForbiddenException("There is no available sandbox definition for particular pool (ID: " + poolId + ").");
            }
            throw new MicroserviceApiException("Error when calling Python API to obtain sandbox definition info for particular pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Lock pool locked pool info.
     *
     * @param poolId the pool id
     * @param accessToken the training access token
     * @return the locked pool info
     */
    public LockedPoolInfo lockPool(Long poolId, String accessToken) {
        try {
            String requestBody = String.format("{\"training_access_token\": \"%s\"}", accessToken);

            return sandboxServiceWebClient
                    .post()
                    .uri("/pools/{poolId}/locks", poolId)
                    .body(Mono.just(requestBody), String.class)
                    .retrieve()
                    .bodyToMono(LockedPoolInfo.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to lock and assign pool with (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Gets pool info.
     *
     * @param poolId the pool id
     * @return the pool info
     */
    public PoolInfoDTO getPoolInfo(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/availability", poolId)
                    .retrieve()
                    .bodyToMono(PoolInfoDTO.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to get pool info (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Unlock pool.
     *
     * @param poolId the pool id
     */
    public void unlockPool(Long poolId) {
        try {
            // get lock id from pool
            PoolInfoDTO poolInfoDto = sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}", poolId)
                    .retrieve()
                    .bodyToMono(PoolInfoDTO.class)
                    .block();
            // unlock pool
            if (poolInfoDto != null && poolInfoDto.getLockId() != null) {
                sandboxServiceWebClient
                        .delete()
                        .uri("/pools/{poolId}/locks/{lockId}", poolId, poolInfoDto.getLockId())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
        } catch (CustomWebClientException ex) {
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND){
                throw new MicroserviceApiException("Currently, it is not possible to unlock a pool (ID: " + poolId + ").", ex);
            }
        }
    }

    /**
     * Get and lock sandbox.
     *
     * @param poolId the pool id
     * @param trainingAccessToken the training access token
     * @return the sandbox info
     */
    public SandboxInfo getAndLockSandbox(Long poolId, String trainingAccessToken) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/sandboxes/get-and-lock/{trainingAccessToken}", poolId, trainingAccessToken)
                    .retrieve()
                    .bodyToMono(SandboxInfo.class)
                    .block();
        } catch (CustomWebClientException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ForbiddenException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
            }
            throw new MicroserviceApiException("Error when calling OpenStack Sandbox Service API to get unlocked sandbox from pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Cleanup sandbox allocation unit and optionally provision a replacement in the same pool.
     *
     * @param allocationUnitId the sandbox allocation unit id
     * @param force            whether the cleanup should delete locked/running sandboxes
     * @param replace          whether a replacement sandbox should be provisioned after cleanup
     */
    public void cleanupSandboxAllocationUnit(Integer allocationUnitId, boolean force, boolean replace) {
        try {
            sandboxServiceWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sandbox-allocation-units/{allocationUnitId}/cleanup-request")
                            .queryParam("force", force)
                            .queryParam("replace", replace)
                            .build(allocationUnitId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException(
                    "Error when calling OpenStack Sandbox Service API to cleanup sandbox allocation unit (ID: "
                            + allocationUnitId + ").", ex);
        }
    }

    /**
     * Get APG variables defined in the sandbox definition of the pool.
     *
     * @param poolId the pool id
     * @return set of APG variables
     */
    public Variables getVariablesByPoolId(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}/variables", poolId)
                    .retrieve()
                    .bodyToMono(Variables.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to get variables of the pool (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Get APG variables defined in the sandbox definition.
     *
     * @param sandboxDefinitionId the sandbox definition id
     * @return set of APG variables
     */
    public Set<String> getVariablesBySandboxDefinitionId(Long sandboxDefinitionId) {
        try {
            return sandboxServiceWebClient
                    .get()
                    .uri("/definitions/{sandboxDefinitionId}/variables", sandboxDefinitionId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to get variables of the sandbox definition (ID: " + sandboxDefinitionId + ").", ex);
        }
    }
}
