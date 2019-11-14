package no.uio.ifi.tc.client;

import kong.unirest.ContentType;
import kong.unirest.HeaderNames;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TSDFileAPIClient {

    public static final String BASE_URL = "https://%sapi.tsd.usit.no/%s/%s%s";
    public static final String BEARER = "Bearer ";

    @Getter
    private Environment environment;

    @Getter
    private String version;

    @Getter
    private String project;

    @Getter
    private String accessKey;

    public String finalizeChunkedUpload(String token, String filename, String uploadId) {
        String url = getURL("/files/stream/" + filename + "?chunk=end&id=" + uploadId);
        return Unirest
                .patch(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asJson()
                .getBody()
                .getObject()
                .getString("id");
    }

    public String uploadChunk(String token, long chunkNumber, byte[] chunk, String filename, String uploadId) {
        String urlString = "/files/stream/" + filename + "?chunk=" + chunkNumber;
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
                .getObject()
                .getString("id");
    }

    public String uploadChunk(String token, long chunkNumber, byte[] chunk, String filename) {
        return uploadChunk(token, chunkNumber, chunk, filename, null);
    }

    public void deleteAllResumableUploads(String token) {
        List<JSONObject> resumableUploads = getResumableUploads(token);
        for (JSONObject resumableUpload : resumableUploads) {
            String uploadId = resumableUpload.getString("id");
            String fileName = resumableUpload.getString("filename");
            deleteResumableUpload(token, fileName, uploadId);
        }
    }

    public String deleteResumableUpload(String token, String fileName, String uploadId) {
        String url = getURL(String.format("/files/resumables/%s?id=%s", fileName, uploadId));
        return Unirest
                .delete(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asString()
                .getBody();
    }

    public List<JSONObject> getResumableUploads(String token) {
        return getResumableUploads(token, null, null);
    }

    public List<JSONObject> getResumableUploadsByFile(String token, String fileName) {
        return getResumableUploads(token, fileName, null);
    }

    public List<JSONObject> getResumableUploadsById(String token, String uploadId) {
        return getResumableUploads(token, null, uploadId);
    }

    @SuppressWarnings("unchecked")
    public List<JSONObject> getResumableUploads(String token, String fileName, String uploadId) {
        String urlString = StringUtils.isEmpty(fileName) ? "/files/resumables" : ("/files/resumables/" + fileName);
        if (StringUtils.isNotEmpty(uploadId)) {
            urlString += "?id=" + uploadId;
        }
        String url = getURL(urlString);
        return Unirest
                .get(url)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .asJson()
                .getBody()
                .getObject()
                .getJSONArray("resumables")
                .toList();
    }

    public String upload(String token, InputStream inputStream, String filename) throws IOException {
        String url = getURL("/files/stream");
        return Unirest
                .put(url)
                .header("Filename", filename)
                .header(HeaderNames.AUTHORIZATION, BEARER + token)
                .body(inputStream.readAllBytes())
                .asString()
                .getBody();
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
                .getString("token");
    }

    public String getToken(TokenType tokenType, String accessKey, String username, String password, String oneTimeCode) {
        String url = getURL("/auth/tsd/token?type=" + tokenType.name().toLowerCase());
        return Unirest
                .post(url)
                .header(HeaderNames.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header(HeaderNames.AUTHORIZATION, BEARER + accessKey)
                .body(String.format("{\"user_name\":\"%s\", \"password\": \"%s\", \"otp\":\"%s\"}", username, password, oneTimeCode))
                .asJson()
                .getBody()
                .getObject()
                .getString("token");
    }

    private String getURL(String endpoint) {
        return String.format(BASE_URL, environment.getEnvironment(), version, project, endpoint);
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

        public TSDFileAPIClient build() {
            TSDFileAPIClient tsdFileAPIClient = new TSDFileAPIClient();
            tsdFileAPIClient.environment = this.environment == null ? DEFAULT_ENVIRONMENT : this.environment;
            tsdFileAPIClient.version = this.version == null ? DEFAULT_VERSION : this.version;
            tsdFileAPIClient.project = this.project == null ? DEFAULT_PROJECT : this.project;
            tsdFileAPIClient.accessKey = this.accessKey;
            return tsdFileAPIClient;
        }

    }

}
