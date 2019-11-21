package no.uio.ifi.tc;

import com.google.gson.Gson;
import kong.unirest.ContentType;
import kong.unirest.HeaderNames;
import kong.unirest.Unirest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.uio.ifi.tc.model.Environment;
import no.uio.ifi.tc.model.TokenType;
import no.uio.ifi.tc.model.pojo.GetResumableUploadResponse;
import no.uio.ifi.tc.model.pojo.TokenResponse;
import no.uio.ifi.tc.model.pojo.UploadResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Main class of the library, encapsulating TSD File API client methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TSDFileAPIClient {

    private static final String BASE_URL = "%s://%s%s/%s/%s%s";
    private static final String BEARER = "Bearer ";

    private Gson gson = new Gson();

    private String protocol;
    private String host;
    private Environment environment;
    private String version;
    private String project;
    private String accessKey;

    /**
     * Uploads streams the input at once, not chunked.
     *
     * @param token       Auth token to use.
     * @param inputStream Stream to send to TSD.
     * @param fileName    File name to use.
     * @return API response.
     * @throws IOException In case of I/O related errors.
     */
    public UploadResponse upload(String token, InputStream inputStream, String fileName) throws IOException {
        String url = getURL("/files/stream");
        String response = Unirest
                .put(url)
                .header("Filename", fileName)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(inputStream.readAllBytes())
                .asString()
                .getBody();
        return gson.fromJson(response, UploadResponse.class);
    }

    /**
     * Lists all initiated and not yet finished resumable uploads.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public GetResumableUploadResponse getResumableUploads(String token) {
        return getResumableUploads(token, null, null);
    }

    /**
     * Lists all initiated and not yet finished resumable uploads by file.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public GetResumableUploadResponse getResumableUploadsByFile(String token, String fileName) {
        return getResumableUploads(token, fileName, null);
    }

    /**
     * Lists all initiated and not yet finished resumable uploads by Upload ID.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public GetResumableUploadResponse getResumableUploadsById(String token, String uploadId) {
        return getResumableUploads(token, null, uploadId);
    }

    /**
     * Lists all initiated and not yet finished resumable uploads by file and Upload ID.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public GetResumableUploadResponse getResumableUploads(String token, String fileName, String uploadId) {
        String urlString = StringUtils.isEmpty(fileName) ? "/files/resumables" : ("/files/resumables/" + fileName);
        if (StringUtils.isNotEmpty(uploadId)) {
            urlString += "?id=" + uploadId;
        }
        String url = getURL(urlString);
        String response = Unirest
                .get(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asJson()
                .getBody()
                .toString();
        return gson.fromJson(response, GetResumableUploadResponse.class);
    }

    /**
     * Uploads the first chunk of data (initializes resumable upload).
     *
     * @param token      Auth token to use.
     * @param firstChunk First chunk of data.
     * @param fileName   File name to use.
     * @return API response.
     */
    public String initializeResumableUpload(String token, byte[] firstChunk, String fileName) {
        return uploadChunk(token, 1, firstChunk, fileName, null);
    }

    /**
     * Upload another chunk of data (NB: chunks must arrive in order).
     *
     * @param token       Auth token to use.
     * @param chunkNumber Order number of the chunk.
     * @param chunk       Chunk of data to upload.
     * @param fileName    File name to use.
     * @param uploadId    Upload ID.
     * @return API response.
     */
    public String uploadChunk(String token, long chunkNumber, byte[] chunk, String fileName, String uploadId) {
        String urlString = "/files/stream/" + fileName + "?chunk=" + chunkNumber;
        if (StringUtils.isNotEmpty(uploadId)) {
            urlString += "&id=" + uploadId;
        }
        String url = getURL(urlString);
        return Unirest
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(chunk)
                .asJson()
                .getBody()
                .toString();
    }

    /**
     * Finalizes resumable upload.
     *
     * @param token    Auth token to use.
     * @param fileName File name to use.
     * @param uploadId Upload ID.
     * @return API response.
     */
    public String finalizeResumableUpload(String token, String fileName, String uploadId) {
        String url = getURL("/files/stream/" + fileName + "?chunk=end&id=" + uploadId);
        return Unirest
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asJson()
                .getBody()
                .toString();
    }

    /**
     * Deletes initiated and not yet finished resumable uploads.
     *
     * @param token    Auth token to use.
     * @param fileName File name to use.
     * @param uploadId Upload ID.
     * @return API response.
     */
    public String deleteResumableUpload(String token, String fileName, String uploadId) {
        String url = getURL(String.format("/files/resumables/%s?id=%s", fileName, uploadId));
        return Unirest
                .delete(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .getBody()
                .toString();
    }

    /**
     * Retrieves the auth token by using basic auth.
     *
     * @param tokenType Type of the token to request.
     * @return API response.
     */
    public TokenResponse getToken(TokenType tokenType) {
        String url = getURL("/auth/basic/token");
        String response = Unirest
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"type\":\"%s\"}", tokenType.name().toLowerCase()))
                .asJson()
                .getBody()
                .toString();
        return gson.fromJson(response, TokenResponse.class);
    }

    /**
     * Retrieves the auth token by using TSD auth.
     *
     * @param tokenType   Type of the token to request.
     * @param accessKey   TSD access key.
     * @param username    Login of the user.
     * @param password    Password of the user.
     * @param oneTimeCode OTP from the authentication device.
     * @return API response.
     */
    public TokenResponse getToken(TokenType tokenType, String accessKey, String username, String password, String oneTimeCode) {
        String url = getURL("/auth/tsd/token?type=" + tokenType.name().toLowerCase());
        String response = Unirest
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"user_name\":\"%s\", \"password\": \"%s\", \"otp\":\"%s\"}", username, password, oneTimeCode))
                .getBody()
                .toString();
        return gson.fromJson(response, TokenResponse.class);
    }

    private String getURL(String endpoint) {
        return String.format(BASE_URL, protocol, environment.getEnvironment(), host, version, project, endpoint);
    }

    /**
     * Class that build the TSDFileAPIClient instance.
     */
    public static class Builder {

        private static final String DEFAULT_HOST = "api.tsd.usit.no";
        private static final Environment DEFAULT_ENVIRONMENT = Environment.PRODUCTION;
        private static final String DEFAULT_VERSION = "v1";
        private static final String DEFAULT_PROJECT = "p11";

        private Boolean secure;
        private String host;
        private Environment environment;
        private String version;
        private String project;
        private String accessKey;

        /**
         * Public parameter-less constructor.
         */
        public Builder() {
        }

        /**
         * Defines whether use HTTP or HTTPS.
         *
         * @param secure <code>true</code> for HTTPS, <code>false</code> otherwise.
         * @return Builder instance.
         */
        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Sets hostname (maybe with port).
         *
         * @param host Hostname (optionally with a port) to work against.
         * @return Builder instance.
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the environment.
         *
         * @param environment Environment to use.
         * @return Builder instance.
         */
        public Builder environment(String environment) {
            this.environment = Environment.get(environment);
            return this;
        }

        /**
         * Sets the version to use.
         *
         * @param version Version of the TSD File API.
         * @return Builder instance.
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the project to use.
         *
         * @param project Project ID in the TSD.
         * @return Builder instance.
         */
        public Builder project(String project) {
            this.project = project;
            return this;
        }

        /**
         * Sets the access key to use.
         *
         * @param accessKey TSD File API access key for Basic Auth.
         * @return Builder instance.
         */
        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        /**
         * Build the client.
         *
         * @return Client.
         */
        public TSDFileAPIClient build() {
            TSDFileAPIClient tsdFileAPIClient = new TSDFileAPIClient();
            tsdFileAPIClient.protocol = this.secure == null ? "https" : (this.secure ? "https" : "http");
            tsdFileAPIClient.host = this.host == null ? DEFAULT_HOST : this.host;
            tsdFileAPIClient.environment = this.environment == null ? DEFAULT_ENVIRONMENT : this.environment;
            tsdFileAPIClient.version = this.version == null ? DEFAULT_VERSION : this.version;
            tsdFileAPIClient.project = this.project == null ? DEFAULT_PROJECT : this.project;
            tsdFileAPIClient.accessKey = this.accessKey;
            return tsdFileAPIClient;
        }

    }

}
