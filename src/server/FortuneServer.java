package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import merrimackutil.json.types.*;
import merrimackutil.json.types.JSONArray;
public class FortuneServer {

  public static void main(String[] args) {
    try {
      ServerSocket server = new ServerSocket(5000);

      File configFile = new File("./config.json");
      JSONArray test= new JSONArray();
      
      // Loop forever handling connections.
      while (true) {
        Socket sock = server.accept();

        System.out.println("Connection received.");

        Scanner recv = new Scanner(sock.getInputStream());
        PrintWriter send = new PrintWriter(sock.getOutputStream(), true);

        while (true) {
          String line = recv.nextLine();

          System.out.println("Client said: " + line);

          Object receivedObject = line;
          if (receivedObject instanceof String) {
            String receivedObject2 = receivedObject.toString();
            if (receivedObject2.startsWith("Message:")) {
              System.out.println("Received message: " + receivedObject2);
            } else if (receivedObject2.startsWith("TYPE:")) {
              if (receivedObject2.endsWith("Random by Author")) {
                System.out.println("Running Random By Author");
              } else if (receivedObject2.endsWith("Random")) {
                System.out.println("Running Standard Random ");
              }
            }
          }

          // Sending something back to client

          send.println("Server: Message received from client - " + line);

          if (line.equalsIgnoreCase("exit")) {
            break;
          }
        }

        // Close the connection.
        sock.close();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
