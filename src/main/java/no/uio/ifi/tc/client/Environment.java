package no.uio.ifi.tc.client;

import lombok.Getter;

import java.util.Arrays;

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
