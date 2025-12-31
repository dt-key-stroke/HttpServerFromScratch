import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
  public static ExecutorService es = Executors.newFixedThreadPool(1);
  
  public static Map<String, String> parseHeaders(String flatHeaders) {
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

  public static void parseRequest(Socket sock, String[] args) throws Exception {
    byte[] buff = new byte[4096];
    Thread.sleep(3000);
    sock.getInputStream().read(buff);
    var fullRequest = new String(buff);
    var requestSplit = fullRequest.split("\r\n");
    String firstLine = requestSplit[0];
    Map<String, String> headers = parseHeaders(fullRequest);
    
    String[] request_line = firstLine.split(" ");
    System.out.println("Total split of header: " + request_line.length);
    String url = request_line[1];
    var url_parts = List.of(url.split("/"));
    if (url.equals("/")) {
      sendResponse(sock, "HTTP/1.1 200 OK\r\n\r\n");
    } else if (url.startsWith("/files/") && url_parts.size() == 3) {
      String directory = args[2];
        Path file_path = Path.of(directory + url_parts.get(2));
        File file = new File(file_path.toUri());
        if (file.exists() && !file.isDirectory()) {
          try (var br = new BufferedReader(new FileReader(file))) {
            var response_body = br.readAllAsString();
            var repsonse_length = response_body.length();
            sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));  
          }
        } else {
          notFound(sock);
        }

    } else if (url.startsWith("/echo/") && url_parts.size() == 3) {
      var param = url.split("/")[2];
      var response_body = param;
      var repsonse_length = response_body.length();
      sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
    } else if (url.equals("/user-agent")) {
      // System.out.println("At user agent");
      // headers.forEach(
      //   (k, v) -> {
      //     System.out.println(String.format("Key: %s, Value: %s", k, v));
      //   }
      // );
      var response_body = headers.getOrDefault("User-Agent", "").strip();
      var repsonse_length = response_body.length();
      sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
    } else {
      notFound(sock);
    }
  }

  public static void notFound(Socket sock) throws IOException {
    System.out.print("NO MATCH!");
    sendResponse(sock, "HTTP/1.1 404 Not Found\r\n\r\n");
  }

  public static void sendResponse(Socket sock, String content) throws IOException {
    try (sock) {
      sock.getOutputStream().write(content.getBytes());
      sock.getOutputStream().flush();
    }
    System.out.println("Sent this: " + content);
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Starting the server...");
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(4221);
    
      serverSocket.setReuseAddress(true);
      while (true) {
        Socket recv = serverSocket.accept();
        System.out.println("Got something..."); 
        Main.es.execute(() -> {
          try {
            parseRequest(recv, args);
            System.out.println("Sent the response");
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    }
  }
}
