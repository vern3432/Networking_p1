package server;

import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;


public class FortuneServer {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(5000);

            // Loop forever handling connections.
            while (true) {
                // Wait for a connection.
                Socket sock = server.accept();

                System.out.println("Connection received.");

                // Setup the streams for use.
                Scanner recv = new Scanner(sock.getInputStream());
                PrintWriter send = new PrintWriter(sock.getOutputStream(), true);

                // Loop to continuously receive and send messages until client disconnects.
                while (true) {
                    // Get the line from the client.
                    String line = recv.nextLine();
                    System.out.println("Client said: " + line);

                    // Echo the line back.
                    send.println(line);

                    // If the client sends an "exit" message, break the loop to close the connection.
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
