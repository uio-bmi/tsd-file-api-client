package no.uio.ifi.tc.client;

import kong.unirest.ContentType;
import kong.unirest.HeaderNames;
import kong.unirest.Unirest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TC {

    public static final String BASE_URL = "https://%sapi.tsd.usit.no/%s/%s%s";
    public static final String BEARER = "Bearer ";
    public static final String TOKEN = "token";

    @Getter
    private Environment environment;

    @Getter
    private String version;

    @Getter
    private String project;

    @Getter
    private String accessKey;

    private String getURL(String endpoint) {
        return String.format(BASE_URL, environment.getEnvironment(), version, project, endpoint);
    }

    public String getToken(TokenType tokenType) {
        String url = getURL("/auth/basic/token");
        return Unirest
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"type\":\"%s\"}", tokenType.name().toLowerCase()))
                .asJson()
                .getBody()
                .getObject()
                .getString(TOKEN);
    }

    public String getToken(TokenType tokenType, String accessKey, String username, String password, String oneTimeCode) {
        String url = getURL("/auth/tsd/token?type=" + tokenType.name().toLowerCase());
        System.out.println("url = " + url);
        return Unirest
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"user_name\":\"%s\", \"password\": \"%s\", \"otp\":\"%s\"}", username, password, oneTimeCode))
                .asJson()
                .getBody()
                .getObject()
                .getString(TOKEN);
    }

    public static class Builder {

        public static Environment DEFAULT_ENVIRONMENT = Environment.PRODUCTION;
        public static String DEFAULT_VERSION = "v1";
        public static String DEFAULT_PROJECT = "p11";

        private Environment environment;
        private String version;
        private String project;
        private String accessKey;

        public Builder() {
        }

        public Builder environment(String environment) {
            this.environment = Environment.get(environment);
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public TC build() {
            TC tc = new TC();
            tc.environment = this.environment == null ? DEFAULT_ENVIRONMENT : this.environment;
            tc.version = this.version == null ? DEFAULT_VERSION : this.version;
            tc.project = this.project == null ? DEFAULT_PROJECT : this.project;
            tc.accessKey = this.accessKey;
            return tc;
        }

    }

}
