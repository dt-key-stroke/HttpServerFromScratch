import lombok.Data;

@Data
public class Response {
    String version;
    int status;
    String statusMessage;
    String responseBody;
}
