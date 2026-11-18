package cz.cyberrange.platform.training.service.enrollment;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Asks user-and-group whether a user is in a group.
 * <p>
 * The group endpoints of user-and-group are for administrators only, so a trainee's own token cannot be used.
 * The client signs in with OAuth2 client credentials ({@code training.user-and-group.service-account.*}, falling
 * back to {@code training.sandbox.service-account.*}). The account must be a user-and-group administrator.
 * Only the access token is cached; membership is asked on every call. Any failure is reported as an error, never as
 * membership, so a misconfiguration denies access instead of opening it.
 */
@Component
public class UserAndGroupMembershipClient implements GroupMembershipClient {

    static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 1000;

    private final WebClient userAndGroupClient;
    private final WebClient tokenClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final Clock clock;

    private String cachedToken;
    private Instant cachedTokenExpiresAt = Instant.EPOCH;

    @Autowired
    public UserAndGroupMembershipClient(
            @Value("${user-and-group-server.uri}") String userAndGroupUri,
            @Value("${training.user-and-group.service-account.token-uri:${training.sandbox.service-account.token-uri:}}") String tokenUri,
            @Value("${training.user-and-group.service-account.client-id:${training.sandbox.service-account.client-id:}}") String clientId,
            @Value("${training.user-and-group.service-account.client-secret:${training.sandbox.service-account.client-secret:}}") String clientSecret) {
        this(WebClient.builder().baseUrl(userAndGroupUri).build(), WebClient.builder().build(),
                tokenUri, clientId, clientSecret, Clock.systemUTC());
    }

    UserAndGroupMembershipClient(WebClient userAndGroupClient, WebClient tokenClient,
                                 String tokenUri, String clientId, String clientSecret, Clock clock) {
        this.userAndGroupClient = userAndGroupClient;
        this.tokenClient = tokenClient;
        this.tokenUri = tokenUri == null ? "" : tokenUri;
        this.clientId = clientId == null ? "" : clientId;
        this.clientSecret = clientSecret == null ? "" : clientSecret;
        this.clock = clock;
    }

    @Override
    public boolean isMember(long groupId, long userRefId) {
        String token = serviceAccountToken();
        for (int page = 0; page < MAX_PAGES; page++) {
            PageResultResource<UserRefDTO> users = usersInGroup(groupId, userRefId, page, token);
            if (users == null || users.getContent() == null) {
                return false;
            }
            if (users.getContent().stream().anyMatch(user -> Long.valueOf(userRefId).equals(user.getUserRefId()))) {
                return true;
            }
            if (users.getPagination() == null || page + 1 >= users.getPagination().getTotalPages()) {
                return false;
            }
        }
        return false;
    }

    private PageResultResource<UserRefDTO> usersInGroup(long groupId, long userRefId, int page, String token) {
        try {
            // "id" narrows the result to the caller when user-and-group applies its Querydsl binding;
            // the loop above still checks every page in case it does not.
            return userAndGroupClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users/groups")
                            .queryParam("ids", groupId)
                            .queryParam("id", userRefId)
                            .queryParam("page", page)
                            .queryParam("size", PAGE_SIZE)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PageResultResource<UserRefDTO>>() {
                    })
                    .block();
        } catch (WebClientException ex) {
            throw new InternalServerErrorException("Could not check group membership in user-and-group.", ex);
        }
    }

    synchronized String serviceAccountToken() {
        Instant now = clock.instant();
        if (cachedToken != null && cachedTokenExpiresAt.isAfter(now.plusSeconds(30))) {
            return cachedToken;
        }
        if (tokenUri.isBlank() || clientId.isBlank() || clientSecret.isBlank()) {
            throw new InternalServerErrorException("Enrollment check needs training.user-and-group.service-account.* "
                    + "(or training.sandbox.service-account.*) to be configured.");
        }
        Map<?, ?> response;
        try {
            response = tokenClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientException ex) {
            throw new InternalServerErrorException("Could not obtain a service-account token for user-and-group.", ex);
        }
        if (response == null || response.get("access_token") == null) {
            throw new InternalServerErrorException("Service-account token response did not contain access_token.");
        }
        long expiresIn = response.get("expires_in") instanceof Number number ? number.longValue() : 60L;
        cachedToken = response.get("access_token").toString();
        cachedTokenExpiresAt = now.plusSeconds(Math.max(expiresIn, 1L));
        return cachedToken;
    }
}
