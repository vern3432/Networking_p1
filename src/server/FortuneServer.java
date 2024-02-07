package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Double;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.*;

public class FortuneServer {

  public static void main(String[] args) {
    try {
      String configFilePath = "src/server/config.json";
      File configFile = new File(configFilePath);
      try (
        BufferedReader br = new BufferedReader(new FileReader(configFilePath))
      ) {
        String line;
        while ((line = br.readLine()) != null) {
          System.out.println(line);
        }
      }
      JSONObject configJsonObject = JsonIO.readObject(configFile);
      String dbFilePath = "src/server/"+configJsonObject.get("fortune-database").toString();
      @SuppressWarnings("deprecation")
      Double port = new Double(configJsonObject.get("port").toString());
      @SuppressWarnings("deprecation")
      Double pool_size = new Double(configJsonObject.get("pool-size").toString());
      int portint = port.intValue();
      int pool_sizeint = pool_size.intValue();

      System.out.println(dbFilePath);
      File dbFile = new File(dbFilePath);
      JSONObject db = JsonIO.readObject(dbFile);

      System.out.println(db.getArray("quotes").getObject(1));




      //start server
      ServerSocket server = new ServerSocket(portint);
      System.out.println("Server Running at port:" + portint);
      // Loop forever handling connections.(Integer)
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
