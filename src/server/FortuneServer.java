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
                Socket sock = server.accept();

                System.out.println("Connection received.");

                Scanner recv = new Scanner(sock.getInputStream());
                PrintWriter send = new PrintWriter(sock.getOutputStream(), true);

                while (true) {
                    String line = recv.nextLine();
                    System.out.println("Client said: " + line);

                    send.println(line);

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
