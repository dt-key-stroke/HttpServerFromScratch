import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

  public static void parseRequest(Socket sock) throws IOException {
    // byte[] buff = new byte[20];
    BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    String firstLine = br.readLine();
    String[] request_line = firstLine.split(" ");
    System.out.println("Able to split headers");
    String url = request_line[1];
    var param = url.split("/")[2];
    var response_body = param;
    var repsonse_length = response_body.length();

    sendResponse(sock, String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s", repsonse_length, response_body));
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
