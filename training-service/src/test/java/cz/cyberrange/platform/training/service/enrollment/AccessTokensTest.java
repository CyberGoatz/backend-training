package cz.cyberrange.platform.training.service.enrollment;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTokensTest {

    @Test
    void strongTokenIsPrefixDashTwentyBase32Characters() {
        String token = AccessTokens.strongToken("vit-netsec");
        assertTrue(token.matches("vit-netsec-[a-z2-7]{20}"), token);
        assertTrue(AccessTokens.isStrongToken(token));
    }

    @Test
    void strongTokensDoNotRepeat() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(AccessTokens.strongToken("p"));
        }
        assertEquals(1000, tokens.size());
    }

    @Test
    void pinTokensAreNotStrong() {
        assertFalse(AccessTokens.isStrongToken("hunter-1234"));
        assertFalse(AccessTokens.isStrongToken(null));
    }

    @Test
    void prefixOfStripsEitherSuffix() {
        assertEquals("hunter", AccessTokens.prefixOf("hunter-1234"));
        assertEquals("vit-netsec", AccessTokens.prefixOf("vit-netsec-abcdefghij234567abcd"));
    }
}
