package cz.cyberrange.platform.training.service.enrollment;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Access tokens of training instances.
 * <p>
 * Open instances keep the historical {@code <prefix>-<4 digit PIN>} format. Instances restricted to a group
 * ({@code allowedGroupId}) get {@code <prefix>-<20 random base32 characters>} (100 bits from {@link SecureRandom}),
 * so the token cannot be guessed.
 */
public final class AccessTokens {

    /** RFC 4648 base32 alphabet, lower case so tokens read like the prefix. */
    static final char[] BASE32_ALPHABET = "abcdefghijklmnopqrstuvwxyz234567".toCharArray();
    public static final int STRONG_SUFFIX_LENGTH = 20;
    static final int PIN_SUFFIX_LENGTH = 4;
    private static final Pattern STRONG_TOKEN = Pattern.compile(".*-[a-z2-7]{" + STRONG_SUFFIX_LENGTH + "}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private AccessTokens() {
    }

    /**
     * @param prefix the prefix chosen by the organizer.
     * @return {@code <prefix>-<20 random base32 characters>}.
     */
    public static String strongToken(String prefix) {
        StringBuilder token = new StringBuilder(prefix.length() + 1 + STRONG_SUFFIX_LENGTH).append(prefix).append('-');
        for (int i = 0; i < STRONG_SUFFIX_LENGTH; i++) {
            token.append(BASE32_ALPHABET[RANDOM.nextInt(BASE32_ALPHABET.length)]);
        }
        return token.toString();
    }

    public static boolean isStrongToken(String token) {
        return token != null && STRONG_TOKEN.matcher(token).matches();
    }

    /**
     * @param token a full access token in either format.
     * @return the prefix the organizer chose, without the generated suffix.
     */
    public static String prefixOf(String token) {
        int suffixLength = isStrongToken(token) ? STRONG_SUFFIX_LENGTH : PIN_SUFFIX_LENGTH;
        return token.substring(0, Math.max(token.length() - suffixLength - 1, 0));
    }
}
