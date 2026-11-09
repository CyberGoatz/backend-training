package cz.cyberrange.platform.training.service.tenancy;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Puts a JWT caller into the security context for tenancy tests.
 */
public final class TenantTestSupport {

    private TenantTestSupport() {
    }

    /**
     * @param organizationClaim value of the organization claim, or null for a caller without the claim.
     * @param authorities       granted authorities of the caller.
     */
    public static void signIn(Object organizationClaim, String... authorities) {
        Jwt.Builder jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        if (organizationClaim != null) {
            jwt.claim(TenantScope.ORGANIZATION_CLAIM, organizationClaim);
        }
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt.build(),
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
    }

    public static void signOut() {
        SecurityContextHolder.clearContext();
    }
}
