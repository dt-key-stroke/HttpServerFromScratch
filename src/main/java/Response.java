import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Response {
    private String httpVersion;
    private int status;
    private String statusMessage;
    private Map<String, String> headers;
    private String responseBody = "";

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
