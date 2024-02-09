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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.*;

public class FortuneServer {

  public static ArrayList<String> getAllAuthors(
    HashMap<String, List<String>> quotesByAuthor
  ) {
    ArrayList<String> authorNames = new ArrayList<>(quotesByAuthor.keySet());
    return authorNames;
  }

  // function  to get a random quote by a specific author

  public static String getRandomAuthor(ArrayList<String> authorNames) {
    return authorNames.get(new Random().nextInt(authorNames.size()));
  }

  public static void sendQuote(String Author, String Quote, PrintWriter send) {
    System.out.println("Sending Quote by:" + Author);
    JSONArray quoteJsonArray = new JSONArray();
    quoteJsonArray.add(Author);
    quoteJsonArray.add(Quote);
    send.println("Quotes_Sent:" + quoteJsonArray.toJSON());
  }

  public static String getRandomQuoteByAuthor(
    HashMap<String, List<String>> quotesByAuthor,
    String author
  ) {
    // get the list of quotes by the specified author
    List<String> quotesList = quotesByAuthor.get(author);

    // if author exists and has quotes, return a random quote
    if (quotesList != null && !quotesList.isEmpty()) {
      Random rand = new Random();
      return quotesList.get(rand.nextInt(quotesList.size()));
    } else {
      return "Author not found or no quotes available";
    }
  }

  public static void main(String[] args) {
    try {
      //reading config flies
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
      int portint = port.intValue();

      int poolSizeInt = 10;

      try {
        @SuppressWarnings("deprecation")
        Double pool_size = new Double(
          configJsonObject.get("pool-size").toString()
        );
        poolSizeInt = pool_size.intValue();

        System.out.println("Using Pool Size:" + poolSizeInt);
      } catch (Exception ex) {
        System.out.println("Pool Size not found. Default: 10");
      }

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
          // if the author does not exist,nor does quote, in the map, create a new list and add the quote to it
          List<String> quotesList = new ArrayList<>();
          quotesList.add(quote);
          quotesByAuthor.put(author, quotesList);
        }
      }
      //printing db by author

      for (Map.Entry<String, List<String>> entry : quotesByAuthor.entrySet()) {
        String author = entry.getKey();
        List<String> quotesList = entry.getValue();
        // System.out.println("Author: " + author);
        // System.out.println("Quotes:");
        // for (String quote : quotesList) {
        //   System.out.println("- " + quote);
        // }
      }

      //arraylist of all authors
      ArrayList<String> authorNames = getAllAuthors(quotesByAuthor);

      //map/values gives you all values as list, just do this as random
      //quotes

      //start server after proccessing
      // port and then pool size
      ServerSocket server = new ServerSocket(portint);

      System.out.println("Server Running at port:" + portint);

      // Create a thread pool
      ExecutorService pool = Executors.newFixedThreadPool(poolSizeInt);

      // loop forever thingy, handling connections.(Integer) dies if connection lost. need to fix that
      while (true) {
        Socket sock = server.accept();

        System.out.println("Connection received.");

        // new connection for each thread
        pool.execute(new Connection_Handler(sock, quotesByAuthor, authorNames));
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  // sub class to handle each indivual connection
  static class Connection_Handler implements Runnable {

    private Socket socket;
    private HashMap<String, List<String>> quotesByAuthor;
    private ArrayList<String> authorNames;

    public Connection_Handler(
      Socket socket,
      HashMap<String, List<String>> quotesByAuthor,
      ArrayList<String> authorNames
    ) {
      this.socket = socket;
      this.quotesByAuthor = quotesByAuthor;
      this.authorNames = authorNames;
    }

    @Override
    public void run() {
      try {
        Scanner recv = new Scanner(socket.getInputStream());
        PrintWriter send = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
          try {
            //recieving and saving next recieved line
            String line = recv.nextLine();
            System.out.println("Client said: " + line);
            Object receivedObject = line;

            if (receivedObject instanceof String) {
              String receivedObject2 = receivedObject.toString();
              if (receivedObject2.startsWith("Message:")) {
                //case for echoe messsage
                send.println("Server: Message received from client - " + line);
                System.out.println("Received message: " + receivedObject2);
              } else if (receivedObject2.startsWith("Author_Request:")) {
                //condition for requesting by author
                receivedObject2 =
                  receivedObject2.replace("Author_Request:", "");
                System.out.println(receivedObject2);
                System.out.println("Author request");
                String Temp = getRandomQuoteByAuthor(
                  quotesByAuthor,
                  receivedObject2
                );
                sendQuote(receivedObject2, Temp, send);
              } else if (receivedObject2.startsWith("TYPE:")) {
                if (receivedObject2.endsWith("Random by Author")) {
                  //requesting author names
                  System.out.println("Running Random By Author");
                  JSONArray authors_JsonArray = new JSONArray();
                  for (int i = 0; i < authorNames.size(); i++) {
                    authors_JsonArray.add(authorNames.get(i));
                  }
                  send.println("Authors_JSON:" + authors_JsonArray.toJSON());
                } else if (receivedObject2.endsWith("Random")) {
                  //requesting normal basic random quote
                  System.out.println("Running Standard Random ");
                  String author = getRandomAuthor(authorNames);
                  String Quote = getRandomQuoteByAuthor(quotesByAuthor, author);
                  sendQuote(author, Quote, send);
                }
              }
            }

            if (line.equalsIgnoreCase("exit")) {
              break;
            }
          } catch (NoSuchElementException e) {
            //exception catch for loss of connection with single user
            System.out.println(
              "Connection lost. Waiting for new connection..."
            );
            break;
          }
        }

        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
