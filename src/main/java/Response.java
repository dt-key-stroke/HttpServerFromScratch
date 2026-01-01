import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Response {
    private String httpVersion;
    private int status;
    private String statusMessage;
    private Map<String, String> headers;
    private String responseBody = "";

    public Response() {
        this.headers = new HashMap<>();
    }

    // Getters and Setters
    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String toFlatResponse() {
        String flatHeaders = "";
        if(this.headers != null && !this.headers.isEmpty()) {
            flatHeaders = this.headers
            .entrySet()
            .stream()
            .map((entry) -> String.format("%s: %s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\r\n"));
        }
        return String.format("%s %d %s\r\n%s\r\n\r\n%s", this.httpVersion, this.status, this.statusMessage, flatHeaders, this.responseBody);
    }
}
