import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

  public static Map<String, String> parseHeaders(String flatHeaders) {
    System.out.println("FH: " + flatHeaders);
    Map<String, String> headers = new HashMap<>();
    for (String header : flatHeaders.split("\r\n")) {
      if (header.contains(":")) {
        String[] hVals = header.split(":");
        headers.put(hVals[0], hVals[1]);
      }
    }
    return headers;
  }

  public static void parseRequest(Socket sock) throws IOException {
    byte[] buff = new byte[4096];
    sock.getInputStream().read(buff);
    var fullRequest = new String(buff);
    var requestSplit = fullRequest.split("\r\n");
    String firstLine = requestSplit[0];
    Map<String, String> headers = parseHeaders(fullRequest);
    
    String[] request_line = firstLine.split(" ");
    String url = request_line[1];
    var url_parts = List.of(url.split("/"));
    if (url.equals("/")) {
      sendResponse(sock, "HTTP/1.1 200 OK\r\n\r\n");
    }
    else if (url.startsWith("/echo/") && url_parts.size() == 3) {
      var param = url.split("/")[2];
      var response_body = param;
      var repsonse_length = response_body.length();
      sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
    } else if (url.equals("/user-agent")) {
      System.out.println("At user agent");
      // headers.forEach(
      //   (k, v) -> {
      //     System.out.println(String.format("Key: %s, Value: %s", k, v));
      //   }
      // );
      var response_body = headers.getOrDefault("User-Agent", "").strip();
      var repsonse_length = response_body.length();
      sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
    } else {
      sendResponse(sock, "HTTP/1.1 404 Not Found\r\n\r\n");
    }
  }

  public static void sendResponse(Socket sock, String content) throws IOException {
    sock.getOutputStream().write(content.getBytes());
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Starting the server...");
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(4221);
    
      serverSocket.setReuseAddress(true);
    
      Socket recv = serverSocket.accept();
      System.out.println("Got something...");

      parseRequest(recv);
      System.out.println("Sent the response connection");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    }
  }
}
