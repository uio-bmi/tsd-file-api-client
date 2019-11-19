package no.uio.ifi.tc;

import java.util.Arrays;

/**
 * Type of the token to request. IMPORT is the default one.
 */
public enum TokenType {

    IMPORT, EXPORT, ADMIN;

    public static TokenType get(String tokenType) {
        return Arrays.stream(TokenType.values()).filter(e -> e.name().equalsIgnoreCase(tokenType)).findAny().orElse(IMPORT);
    }

}
