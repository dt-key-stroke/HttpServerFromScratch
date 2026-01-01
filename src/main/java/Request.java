import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {
    HttpMethod httpMethod;
    String httpVersion;
    Map<String, String> headers;
    String urlPath;
    String body;

    // Getters and Setters
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static Request fromInputStream(InputStream is) throws Exception {
        byte[] buff = new byte[4096];
        is.read(buff);
        var fullRequest = new String(buff);
        var requestSplit = fullRequest.split("\r\n");
        String firstLine = requestSplit[0];
        Map<String, String> headers = parseHeaders(fullRequest);
        String[] request_line = firstLine.split(" ");
        System.out.println("Total split of header: " + request_line.length);
        String url = request_line[1].strip();
        Request req = new Request();
        req.headers = headers;
        req.httpMethod = HttpMethod.byName(request_line[0].strip());
        req.urlPath = url;
        req.httpVersion = request_line[2].strip();
        var cl = req.headers.getOrDefault("Content-Length", "0");
        var full_req = requestSplit[requestSplit.length-1];
        req.body = full_req.substring(0, Integer.parseInt(cl));

        return req;
    }

    private static Map<String, String> parseHeaders(String flatHeaders) {
        // System.out.println("FH: " + flatHeaders);
        Map<String, String> headers = new HashMap<>();
        for (String header : flatHeaders.split("\r\n")) {
            if (header.contains(":")) {
            String[] hVals = header.split(":");
            headers.put(hVals[0].strip(), hVals[1].strip());
            }
        }
        return headers;
    }


}
