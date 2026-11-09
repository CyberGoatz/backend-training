package cz.cyberrange.platform.training.service.enrollment;

import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAndGroupMembershipClientTest {

    private final AtomicInteger tokenRequests = new AtomicInteger();
    private final List<ClientRequest> uagRequests = new ArrayList<>();

    @Test
    void memberIsFoundWithServiceAccountToken() {
        UserAndGroupMembershipClient client = client(page(1, "{\"id\": 7, \"sub\": \"s\", \"iss\": \"https://idp\", \"mail\": \"a@b.c\", \"full_name\": \"A B\"}"));
        assertTrue(client.isMember(12L, 7L));
        ClientRequest request = uagRequests.get(0);
        assertEquals("Bearer svc-token", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("/user-and-group/api/v1/users/groups", request.url().getPath());
        assertTrue(request.url().getQuery().contains("ids=12"));
        assertTrue(request.url().getQuery().contains("id=7"));
    }

    @Test
    void nonMemberIsNotFound() {
        assertFalse(client(page(1)).isMember(12L, 7L));
    }

    @Test
    void otherUsersInTheResultDoNotCount() {
        assertFalse(client(page(1, "{\"id\": 8}", "{\"id\": 9}")).isMember(12L, 7L));
    }

    @Test
    void laterPagesAreCheckedWhenTheResultIsNotNarrowed() {
        assertTrue(client(page(2, "{\"id\": 8}"), page(2, "{\"id\": 7}")).isMember(12L, 7L));
        assertEquals(2, uagRequests.size());
    }

    @Test
    void tokenIsReusedButMembershipIsAskedEveryTime() {
        UserAndGroupMembershipClient client = client(page(1, "{\"id\": 7}"), page(1));
        assertTrue(client.isMember(12L, 7L));
        assertFalse(client.isMember(12L, 7L));
        assertEquals(1, tokenRequests.get());
        assertEquals(2, uagRequests.size());
    }

    @Test
    void userAndGroupErrorsDenyInsteadOfAllowing() {
        UserAndGroupMembershipClient client = new UserAndGroupMembershipClient(
                uagClient(request -> Mono.just(ClientResponse.create(HttpStatus.FORBIDDEN).build())),
                tokenClient(), "http://idp/token", "academy-provisioner", "secret", Clock.systemUTC());
        assertThrows(InternalServerErrorException.class, () -> client.isMember(12L, 7L));
    }

    @Test
    void missingServiceAccountConfigurationDenies() {
        UserAndGroupMembershipClient client = new UserAndGroupMembershipClient(
                uagClient(request -> Mono.error(new AssertionError("must not call user-and-group"))),
                tokenClient(), "", "", "", Clock.systemUTC());
        assertThrows(InternalServerErrorException.class, () -> client.isMember(12L, 7L));
    }

    private UserAndGroupMembershipClient client(String... pages) {
        AtomicInteger next = new AtomicInteger();
        return new UserAndGroupMembershipClient(
                uagClient(request -> json(pages[Math.min(next.getAndIncrement(), pages.length - 1)])),
                tokenClient(), "http://idp/token", "academy-provisioner", "secret", Clock.systemUTC());
    }

    private WebClient uagClient(ExchangeFunction responder) {
        return WebClient.builder()
                .baseUrl("http://uag/user-and-group/api/v1")
                .exchangeFunction(request -> {
                    uagRequests.add(request);
                    return responder.exchange(request);
                })
                .build();
    }

    private WebClient tokenClient() {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    tokenRequests.incrementAndGet();
                    return json("{\"access_token\": \"svc-token\", \"expires_in\": 300}");
                })
                .build();
    }

    private static String page(int totalPages, String... users) {
        return "{\"content\": [" + String.join(",", users) + "], \"pagination\": {\"number\": 0, \"number_of_elements\": "
                + users.length + ", \"size\": 100, \"total_elements\": " + users.length + ", \"total_pages\": " + totalPages + "}}";
    }

    private static Mono<ClientResponse> json(String body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
    }
}
