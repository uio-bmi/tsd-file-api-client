package no.uio.ifi.tc;

import lombok.Getter;

import java.util.Arrays;

/**
 * Type of the environment to work against. PRODUCTION is the default one.
 */
public enum Environment {

    PRODUCTION(""), INTERNAL("internal."), TESTING("test.");

    @Getter
    private String environment;

    Environment(String environment) {
        this.environment = environment;
    }

    public static Environment get(String environment) {
        return Arrays.stream(Environment.values()).filter(e -> e.name().equalsIgnoreCase(environment)).findAny().orElse(PRODUCTION);
    }

}
