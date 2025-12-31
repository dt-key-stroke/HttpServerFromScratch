import java.io.IOException;
import java.net.ServerSocket;

public class Main {
  public static void main(String[] args) {
    System.out.println("Starting the server...");

    try {
      ServerSocket serverSocket = new ServerSocket(4221);
    
      serverSocket.setReuseAddress(true);
    
      Socket recv = serverSocket.accept(); // Wait for connection from client.
      System.out.println("Got something...");
      byte[] buff = new byte[20];
      recv.getInputStream().read(buff);
      String request = new String(buff);
      System.out.println("got a str");
      String[] split_request = request.split("\r\n");
      String[] request_line = split_request[0].split(" ");
      String url = request_line[1];

      System.out.println("inp_str: " + request);
      System.out.println("req_line: " + request_line);
      System.out.println("req_split: " + split_request);
      if(url.equals("/")) {
        recv.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      } else {
        recv.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      }
      System.out.println("Sent the response connection");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
