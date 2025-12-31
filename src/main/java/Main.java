import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
  public static ExecutorService es = Executors.newFixedThreadPool(10);
  

  public static void parseRequest(Socket sock, String[] args) throws Exception {
    Request req = Request.fromInputStream(sock.getInputStream());
    var url_parts = List.of(req.urlPath.split("/"));

    if (req.urlPath.equals("/")) {
      sendResponse(sock, "HTTP/1.1 200 OK\r\n\r\n");

    } else if (req.urlPath.startsWith("/files/") && url_parts.size() == 3) {
      if (req.httpMethod.equals(HttpMethod.GET)) {

        String directory = args[1];
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
      } else if (req.httpMethod.equals(HttpMethod.POST)) {
        String directory = args[1];
        Path file_path = Path.of(directory + url_parts.get(2));
        Files.createFile(file_path);
        File file = new File(file_path.toUri());
        file.setWritable(true);
        try (var pw = new PrintWriter(file)) {
          pw.write(req.body);
        }
      }

    } else if (req.urlPath.startsWith("/echo/") && url_parts.size() == 3) {
      var param = req.urlPath.split("/")[2];
      var response_body = param;
      var repsonse_length = response_body.length();
      sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
    } else if (req.urlPath.equals("/user-agent")) {
      // System.out.println("At user agent");
      // headers.forEach(
      //   (k, v) -> {
      //     System.out.println(String.format("Key: %s, Value: %s", k, v));
      //   }
      // );
      var response_body = req.headers.getOrDefault("User-Agent", "").strip();
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
