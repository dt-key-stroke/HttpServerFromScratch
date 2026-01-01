import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class Main {
  public static ExecutorService es = Executors.newFixedThreadPool(10);
  

  public static void parseRequest(Socket sock, String[] args) throws Exception {
    Request req = Request.fromInputStream(sock.getInputStream());
    var url_parts = List.of(req.urlPath.split("/"));

    if (req.urlPath.equals("/")) {
        Response res = new Response();
        res.setHttpVersion(req.httpVersion);
        res.setStatus(200);
        res.setStatusMessage("OK");
        sendResponse(sock, res.toFlatResponse());
      } else if (req.urlPath.startsWith("/files/") && url_parts.size() == 3) {
        if (req.httpMethod.equals(HttpMethod.GET)) {
          
          String directory = args[1];
          Path file_path = Path.of(directory + url_parts.get(2));
          File file = new File(file_path.toUri());
          if (file.exists() && !file.isDirectory()) {
            try (var br = new BufferedReader(new FileReader(file))) {
              var response_body = br.readAllAsString();
              var repsonse_length = response_body.length();
              Response res = new Response();
              res.setHeaders(Map.of(
                "Content-Type", "application/octet-stream", 
                "Content-Length", String.valueOf(repsonse_length)
              )
            );
            res.setHttpVersion(req.httpVersion);
            res.setStatus(200);
            res.setStatusMessage("OK");
            res.setResponseBody(response_body);
            sendResponse(sock, res.toFlatResponse());  
          }
        } else {
          notFound(sock);
        }
      } else if (req.httpMethod.equals(HttpMethod.POST)) {
        String directory = args[1];
        Path file_path = Path.of(directory + url_parts.get(2));
        Files.createFile(file_path);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(file_path.toString()), "utf-8"))) {
            writer.write(req.body);
          }
          Response res = new Response();
          res.setHttpVersion(req.httpVersion);
          res.setStatus(201);
          res.setStatusMessage("Created");
          sendResponse(sock, res.toFlatResponse());
        }
        
      } else if (req.urlPath.startsWith("/echo/") && url_parts.size() == 3) {
        var param = req.urlPath.split("/")[2];
        var response_body = param;
        var repsonse_length = response_body.length();
        List<String> req_enc = List.of(req.headers.getOrDefault("Accept-Encoding", "").split(","));
        req_enc = req_enc.stream().map(String::strip).collect(Collectors.toList());
        Response res = new Response();
        Map<String, String> m = new HashMap<>();
        m.putAll(Map.of(
          "Content-Type", "text/plain", 
          "Content-Length", String.valueOf(repsonse_length)
        ));
        res.setHeaders(m);
        res.setHttpVersion(req.httpVersion);
        res.setStatus(200);
        res.setStatusMessage("OK");
      if (req_enc.contains("gzip")) {
        res.addHeader("Content-Encoding", "gzip");
        byte[] compressed_payload = Compression.gzip(response_body);
        res.setResponseBody("");
        res.addHeader("Content-Length", String.valueOf(compressed_payload.length));
        sendResponse(sock, Misc.concat(res.toFlatResponse().getBytes(), compressed_payload));
        return;
      } else {
        res.setResponseBody(response_body);
      }
      System.out.println("RESPONSE: " + res.toFlatResponse());
      sendResponse(sock, res.toFlatResponse());
    } else if (req.urlPath.equals("/user-agent")) {
      var response_body = req.headers.getOrDefault("User-Agent", "").strip();
      var repsonse_length = response_body.length();
      Response res = new Response();
      res.setHeaders(Map.of(
        "Content-Type", "text/plain", 
        "Content-Length", String.valueOf(repsonse_length)
        )
      );
      res.setHttpVersion(req.httpVersion);
      res.setStatus(200);
      res.setStatusMessage("OK");
      res.setResponseBody(response_body);
      sendResponse(sock, res.toFlatResponse());
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
  }

  public static void sendResponse(Socket sock, byte[] content) throws IOException {
    try (sock) {
      sock.getOutputStream().write(content);
      sock.getOutputStream().flush();
    }
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
