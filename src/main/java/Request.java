import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Request {
    HttpMethod httpMethod;
    Map<String, String> headers;
    String urlPath;
    String body;

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
        req.body = new String(requestSplit[requestSplit.length-1].strip().getBytes(Charset.defaultCharset()), Charset.forName("UTF-8"));
        return req;
    }

    protected  static Map<String, String> parseHeaders(String flatHeaders) {
        // System.out.println("FH: " + flatHeaders);
        Map<String, String> headers = new HashMap<>();
        for (String header : flatHeaders.split("\r\n")) {
            if (header.contains(":")) {
            String[] hVals = header.split(":");
            headers.put(hVals[0], hVals[1]);
            }
        }
        return headers;
    }

}
