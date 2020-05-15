package no.uio.ifi.tc;

import com.auth0.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import kong.unirest.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.tc.model.Environment;
import no.uio.ifi.tc.model.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Main class of the library, encapsulating TSD File API client methods.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TSDFileAPIClient {

    private static final String BASE_URL = "%s://%s%s/%s/%s%s";
    private static final String BEARER = "Bearer ";
    private static final String USER_CLAIM = "user";
    private static final String ID_TOKEN = "idtoken";

    private final Gson gson = new Gson();

    private UnirestInstance unirestInstance;

    private String protocol;
    private String host;
    private Environment environment;
    private String version;
    private String project;
    private String appId;
    private String accessKey;

    /**
     * Lists uploaded files.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public Message listFiles(String token) {
        String url = getURL(getEndpoint(token, "/files/"));
        HttpResponse<String> response = unirestInstance
                .get(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString();
        Message message = new Message();
        try {
            message = gson.fromJson(response.getBody(), Message.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        message.setStatusCode(response.getStatus());
        message.setStatusText(response.getStatusText());
        return message;
    }

    /**
     * Streams the input at once, not chunked.
     *
     * @param token       Auth token to use.
     * @param inputStream Stream to send to TSD.
     * @param fileName    File name to use.
     * @return API response.
     * @throws IOException In case of I/O related errors.
     */
    public Message uploadFile(String token, InputStream inputStream, String fileName) throws IOException {
        String url = getURL(getEndpoint(token, "/files/" + fileName));
        HttpResponse<String> response = unirestInstance
                .put(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(inputStream.readAllBytes())
                .asString();
        Message message = new Message();
        try {
            message = gson.fromJson(response.getBody(), Message.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        message.setStatusCode(response.getStatus());
        message.setStatusText(response.getStatusText());
        return message;
    }

    /**
     * Deletes uploaded file.
     *
     * @param token    Auth token to use.
     * @param fileName File name to use.
     * @return API response.
     */
    public Message deleteFile(String token, String fileName) {
        String url = getURL(getEndpoint(token, "/files/" + fileName));
        HttpResponse<String> response = unirestInstance
                .delete(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString();
        Message message = new Message();
        try {
            message = gson.fromJson(response.getBody(), Message.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        message.setStatusCode(response.getStatus());
        message.setStatusText(response.getStatusText());
        return message;
    }

    /**
     * Lists all initiated and not yet finished resumable uploads by file and Upload ID.
     *
     * @param token Auth token to use.
     * @return API response.
     */
    public ResumableUploads listResumableUploads(String token) {
        String url = getURL(getEndpoint(token, "/resumables"));
        HttpResponse<String> response = unirestInstance
                .get(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString();
        ResumableUploads resumableUploads = new ResumableUploads();
        try {
            resumableUploads = gson.fromJson(response.getBody(), ResumableUploads.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        resumableUploads.setStatusCode(response.getStatus());
        resumableUploads.setStatusText(response.getStatusText());
        return resumableUploads;
    }

    /**
     * Lists all initiated and not yet finished resumable uploads by file and Upload ID.
     *
     * @param token    Auth token to use.
     * @param uploadId Resumable upload ID.
     * @return API response.
     */
    public Optional<ResumableUpload> getResumableUpload(String token, String uploadId) {
        ResumableUploads resumableUploads = listResumableUploads(token);
        Optional<ResumableUpload> resumableUpload = resumableUploads.getResumables().stream().filter(u -> u.getId().equalsIgnoreCase(uploadId)).findAny();
        resumableUpload.ifPresent(r -> r.setStatusCode(resumableUploads.getStatusCode()));
        resumableUpload.ifPresent(r -> r.setStatusText(resumableUploads.getStatusText()));
        return resumableUpload;
    }

    /**
     * Uploads the first chunk of data (initializes resumable upload).
     *
     * @param token      Auth token to use.
     * @param firstChunk First chunk of data.
     * @param fileName   File name to use.
     * @return API response.
     */
    public Chunk initializeResumableUpload(String token, byte[] firstChunk, String fileName) {
        String url = getURL(getEndpoint(token, "/files/" + fileName + "?chunk=1"));
        HttpResponse<String> response = unirestInstance
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(firstChunk)
                .asString();
        Chunk chunkResponse = new Chunk();
        try {
            chunkResponse = gson.fromJson(response.getBody(), Chunk.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        chunkResponse.setStatusCode(response.getStatus());
        chunkResponse.setStatusText(response.getStatusText());
        return chunkResponse;
    }

    /**
     * Upload another chunk of data (NB: chunks must arrive in order).
     *
     * @param token       Auth token to use.
     * @param chunkNumber Order number of the chunk.
     * @param chunk       Chunk of data to upload.
     * @param uploadId    Upload ID.
     * @return API response.
     */
    public Chunk uploadChunk(String token, long chunkNumber, byte[] chunk, String uploadId) {
        ResumableUpload resumableUpload = getResumableUpload(token, uploadId).orElseThrow();
        String url = getURL(getEndpoint(token, "/files/" + resumableUpload.getFileName() + "?chunk=" + chunkNumber + "&id=" + uploadId));
        HttpResponse<String> response = unirestInstance
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(chunk)
                .asString();
        Chunk chunkResponse = new Chunk();
        try {
            chunkResponse = gson.fromJson(response.getBody(), Chunk.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        chunkResponse.setStatusCode(response.getStatus());
        chunkResponse.setStatusText(response.getStatusText());
        return chunkResponse;
    }

    /**
     * Finalizes resumable upload.
     *
     * @param token    Auth token to use.
     * @param uploadId Upload ID.
     * @return API response.
     */
    public Chunk finalizeResumableUpload(String token, String uploadId) {
        ResumableUpload resumableUpload = getResumableUpload(token, uploadId).orElseThrow();
        String url = getURL(getEndpoint(token, "/files/" + resumableUpload.getFileName() + "?chunk=end&id=" + uploadId));
        HttpResponse<String> response = unirestInstance
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString();
        Chunk chunkResponse = new Chunk();
        try {
            chunkResponse = gson.fromJson(response.getBody(), Chunk.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        chunkResponse.setStatusCode(response.getStatus());
        chunkResponse.setStatusText(response.getStatusText());
        return chunkResponse;
    }

    /**
     * Deletes initiated and not yet finished resumable upload.
     *
     * @param token    Auth token to use.
     * @param uploadId Upload ID.
     * @return API response.
     */
    public Message deleteResumableUpload(String token, String uploadId) {
        ResumableUpload resumableUpload = getResumableUpload(token, uploadId).orElseThrow();
        String url = getURL(getEndpoint(token, "/resumables/" + resumableUpload.getFileName() + "?id=" + uploadId));
        HttpResponse<String> response = unirestInstance
                .delete(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString();
        Message message = new Message();
        try {
            message = gson.fromJson(response.getBody(), Message.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        message.setStatusCode(response.getStatus());
        message.setStatusText(response.getStatusText());
        return message;
    }

    /**
     * Retrieves the auth token by using non-TSD identity (OIDC provided).
     *
     * @param tokenType    Type of the token to request.
     * @param oidcProvider OIDC provider name.
     * @param idToken      ID token obtained from the OIDC provider.
     * @return API response.
     */
    public Token getToken(String tokenType, String oidcProvider, String idToken) {
        String url = getURL(String.format("/auth/%s/token?type=", oidcProvider.toLowerCase()) + tokenType.toLowerCase());
        HttpResponse<String> response = unirestInstance
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"%s\":\"%s\"}", ID_TOKEN, idToken))
                .asString();
        Token token = new Token();
        try {
            token = gson.fromJson(response.getBody(), Token.class);
        } catch (JsonSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        token.setStatusCode(response.getStatus());
        token.setStatusText(response.getStatusText());
        return token;
    }

    private String getEndpoint(String token, String path) {
        return String.format("/%s/%s%s", appId, JWT.decode(token).getClaim(USER_CLAIM).asString(), path);
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

        private HttpClient httpClient;
        private String clientCertificateStore;
        private String clientCertificateStorePassword;
        private Boolean secure;
        private Boolean checkCertificate;
        private String host;
        private Environment environment;
        private String version;
        private String project;
        private String appId;
        private String accessKey;

        /**
         * Public parameter-less constructor.
         */
        public Builder() {
        }

        /**
         * Sets custom HTTP client.
         *
         * @param httpClient Apache HTTP client.
         * @return Builder instance.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets client certificate store location (PKCS#12) along with its password.
         *
         * @param clientCertificateStore         Client certificate store location.
         * @param clientCertificateStorePassword client certificate store password.
         * @return Builder instance.
         */
        public Builder clientCertificateStore(String clientCertificateStore, String clientCertificateStorePassword) {
            this.clientCertificateStore = clientCertificateStore;
            this.clientCertificateStorePassword = clientCertificateStorePassword;
            return this;
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
         * Defines whether certificate will be checked in case of HTTPS connection.
         *
         * @param checkCertificate <code>true</code> for checking, <code>false</code> otherwise.
         * @return Builder instance.
         */
        public Builder checkCertificate(boolean checkCertificate) {
            this.checkCertificate = checkCertificate;
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
         * Sets the application ID to use.
         *
         * @param appId Application ID in the TSD.
         * @return Builder instance.
         */
        public Builder appId(String appId) {
            this.appId = appId;
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
            if (httpClient != null) {
                //noinspection deprecation
                Unirest.config().httpClient(httpClient);
            }
            if (StringUtils.isNotEmpty(clientCertificateStore) && StringUtils.isNotEmpty(clientCertificateStorePassword)) {
                Unirest.config().clientCertificateStore(clientCertificateStore, clientCertificateStorePassword);
            }
            TSDFileAPIClient tsdFileAPIClient = new TSDFileAPIClient();
            tsdFileAPIClient.unirestInstance = Unirest.primaryInstance();
            tsdFileAPIClient.protocol = this.secure == null ? "https" : (this.secure ? "https" : "http");
            tsdFileAPIClient.unirestInstance.config().verifySsl(this.checkCertificate == null ? true : this.checkCertificate);
            tsdFileAPIClient.host = this.host == null ? DEFAULT_HOST : this.host;
            tsdFileAPIClient.environment = this.environment == null ? DEFAULT_ENVIRONMENT : this.environment;
            tsdFileAPIClient.version = this.version == null ? DEFAULT_VERSION : this.version;
            tsdFileAPIClient.project = this.project == null ? DEFAULT_PROJECT : this.project;
            if (StringUtils.isEmpty(this.appId)) {
                throw new IllegalArgumentException("Application ID must be set");
            }
            tsdFileAPIClient.appId = this.appId;
            if (StringUtils.isEmpty(this.accessKey)) {
                throw new IllegalArgumentException("Access key must be set");
            }
            tsdFileAPIClient.accessKey = this.accessKey;
            return tsdFileAPIClient;
        }

    }

}
