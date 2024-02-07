package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Double;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.*;

public class FortuneServer {

  public static ArrayList<String> getAllAuthors(
    HashMap<String, List<String>> quotesByAuthor
  ) {
    ArrayList<String> authorNames = new ArrayList<>(quotesByAuthor.keySet());
    return authorNames;
  }

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
      String dbFilePath =
        "src/server/" + configJsonObject.get("fortune-database").toString();
      @SuppressWarnings("deprecation")
      Double port = new Double(configJsonObject.get("port").toString());
      @SuppressWarnings("deprecation")
      Double pool_size = new Double(
        configJsonObject.get("pool-size").toString()
      );
      int portint = port.intValue();
      int pool_sizeint = pool_size.intValue();
      System.out.println(dbFilePath);
      File dbFile = new File(dbFilePath);
      JSONObject db = JsonIO.readObject(dbFile);
      Object[] arraydb = db.getArray("quotes").toArray();
      HashMap<String, List<String>> quotesByAuthor = new HashMap<>();

      for (Object obj : arraydb) {
        JSONObject quoteObject = (JSONObject) obj;
        String author = quoteObject.getString("author");
        String quote = quoteObject.getString("quote");

        // see if the quote already exists for the author
        if (quotesByAuthor.containsKey(author)) {
          List<String> quotesList = quotesByAuthor.get(author);
          // check if the quote already exists in the list
          if (!quotesList.contains(quote)) {
            quotesList.add(quote);
          }
        } else {
          // if the author does not exist in the map, create a new list and add the quote to it
          List<String> quotesList = new ArrayList<>();
          quotesList.add(quote);
          quotesByAuthor.put(author, quotesList);
        }
      }
      //printing db by author
      for (Map.Entry<String, List<String>> entry : quotesByAuthor.entrySet()) {
        String author = entry.getKey();
        List<String> quotesList = entry.getValue();

        System.out.println("Author: " + author);
        System.out.println("Quotes:");
        for (String quote : quotesList) {
          System.out.println("- " + quote);
        }
      }
      ArrayList<String> authorNames = getAllAuthors(quotesByAuthor);


      //map/values gives you all values as list, just do this as random
      //quotes

      //start server after proccessing
      ServerSocket server = new ServerSocket(portint);
      System.out.println("Server Running at port:" + portint);
      // loop forever handling connections.(Integer)
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