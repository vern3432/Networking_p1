package client;

import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;

public class FortuneClient extends JFrame {

  private JTextField messageField;
  private JTextArea responseArea;
  private JComboBox<String> optionsComboBox;
  private Socket socket;
  private Scanner receiver;
  private PrintWriter sender;

  public FortuneClient() {
    setTitle("Fortune Client");
    setSize(400, 300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    getContentPane().add(panel);
    panel.setLayout(null);

    JLabel messageLabel = new JLabel("Echoe Serversock Message");
    messageLabel.setBounds(20, 20, 150, 20);
    panel.add(messageLabel);

    messageField = new JTextField();
    messageField.setBounds(180, 20, 180, 20);
    panel.add(messageField);

    optionsComboBox =
      new JComboBox<>(new String[] { "Random", "Random by Author" });
    optionsComboBox.setBounds(20, 50, 150, 20);
    panel.add(optionsComboBox);

    JButton sendMessageButton = new JButton("Send Message");
    sendMessageButton.setBounds(20, 80, 120, 20);
    sendMessageButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendMessage();
        }
      }
    );
    panel.add(sendMessageButton);

    JButton sendTypeButton = new JButton("Send Type");
    sendTypeButton.setBounds(150, 80, 120, 20);
    sendTypeButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendType();
        }
      }
    );
    panel.add(sendTypeButton);

    responseArea = new JTextArea();
    responseArea.setBounds(20, 110, 340, 130);
    responseArea.setEditable(false);
    panel.add(responseArea);
  }

  private void sendMessage() {
    String message = messageField.getText();
    String selectedOption = (String) optionsComboBox.getSelectedItem();
    if (!message.isEmpty()) {
      sender.println("Message:" + message);
      showSuccessMessage();
      // Receive response from server and display it
      String response = receiver.nextLine();
      // Storing the received message as an object
      Object receivedObject = response;
      // Printing the received message
      System.out.println("Received message: " + receivedObject);
      displayResponse(response);
    } else {
      JOptionPane.showMessageDialog(
        this,
        "Please enter a message.",
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  private void sendType() {
    String selectedOption = (String) optionsComboBox.getSelectedItem();
    sender.println("TYPE:" + selectedOption);
    showSuccessMessage();
    // Receive response from server and display it
    String response = receiver.nextLine();
    // Storing the received message as an object
    Object receivedObject = response;
    // Printing the received message
    System.out.println("Received message: " + receivedObject);
    displayResponse(response);
  }

  private void showSuccessMessage() {
    JOptionPane.showMessageDialog(
      this,
      "Request sent successfully!",
      "Success",
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  private void displayResponse(String response) {
    responseArea.append(response + "\n");
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          FortuneClient client = new FortuneClient();
          client.setVisible(true);
          client.connectToServer();
        }
      }
    );
  }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 5000);
            receiver = new Scanner(socket.getInputStream());
            sender = new PrintWriter(socket.getOutputStream(), true);

            new Thread(this::recieveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recieveMessages(){
        try{
            while(receiver.hasNextLine()){
                String response = receiver.nextLine();
                SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void dispose(){
        super.dispose();
        try{
            if(socket != null) socket.close();
            if(sender != null) sender.close();
            if(receiver != null) receiver.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
