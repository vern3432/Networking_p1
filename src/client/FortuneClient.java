package client;

import java.awt.Image;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import merrimackutil.json.*;
import merrimackutil.json.JsonIO;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

public class FortuneClient extends JFrame {

  private JTextField messageField;
  private JTextArea responseArea;
  private JComboBox<String> optionsComboBox;
  private JComboBox<String> authorsComboBox; 
  private JButton sendAuthorButton; 
  private Socket socket;
  private Scanner receiver;
  private PrintWriter sender;

  public FortuneClient() {
    setTitle("Fortune Client");
    setSize(400, 550);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();

    getContentPane().add(panel);

    panel.setLayout(null);

    JLabel messageLabel = new JLabel("Echo Server");
    messageLabel.setBounds(20, 20, 150, 20);
    panel.add(messageLabel);

    messageField = new JTextField();
    messageField.setBounds(180, 20, 180, 20);
    panel.add(messageField);

    optionsComboBox =
      new JComboBox<>(new String[] { "Random", "Random by Author" });
    optionsComboBox.setBounds(20, 50, 150, 20);
    panel.add(optionsComboBox);

    authorsComboBox = new JComboBox<>(); // Dropdown menu for authors
    authorsComboBox.setBounds(20, 80, 150, 20);
    authorsComboBox.setEnabled(false); // Disabled initially
    panel.add(authorsComboBox);

    sendAuthorButton = new JButton("Send Author"); // Button to send author name
    sendAuthorButton.setBounds(180, 80, 120, 20);
    sendAuthorButton.setEnabled(false); // Disabled initially
    sendAuthorButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendAuthor();
        }
      }
    );
    panel.add(sendAuthorButton);

    JButton sendMessageButton = new JButton("Send Echo");
    sendMessageButton.setBounds(20, 110, 120, 20);
    sendMessageButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendMessage();
        }
      }
    );
    panel.add(sendMessageButton);

    JButton sendTypeButton = new JButton("Send Type");
    sendTypeButton.setBounds(180, 110, 120, 20);
    sendTypeButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendType();
        }
      }
    );
    panel.add(sendTypeButton);

    responseArea = new JTextArea();
    responseArea.setBounds(20, 140, 340, 370);
    responseArea.setLineWrap(true);
    responseArea.setWrapStyleWord(true);
    responseArea.setEditable(false);
    panel.add(responseArea);
  }

  private void sendMessage() {
    String message = messageField.getText();
    String selectedOption = (String) optionsComboBox.getSelectedItem();
    if (!message.isEmpty()) {
      sender.println("Message:" + message);
      showSuccessMessage();
      String response = receiver.nextLine();
      Object receivedObject = response;
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
    String response = receiver.nextLine();
    String receivedObject = response;

    if (receivedObject.startsWith("Authors_JSON:")) {
      System.out.println("Authors received");
      receivedObject = receivedObject.replace("Authors_JSON:", "");
      // JSONParser parser = new JSONParser(receivedObject);
      JSONArray authorsJsonArray = JsonIO.readArray(receivedObject);
      ArrayList<String> authorsList = new ArrayList<>();
      for (int i = 0; i < authorsJsonArray.size(); i++) {
        authorsList.add(authorsJsonArray.get(i).toString());
      }
      Collections.sort(authorsList);
      authorsComboBox.removeAllItems();
      for (String author : authorsList) {
        authorsComboBox.addItem(author);
      }
      authorsComboBox.setEnabled(true); // Enable the author selection
      sendAuthorButton.setEnabled(true); // Enable the send author button
    } else if (receivedObject.startsWith("Quotes_Sent:")) {
      
      receivedObject = receivedObject.replace("Quotes_Sent:", "");
      JSONArray authorsJsonArray = JsonIO.readArray(receivedObject);
      ArrayList<String> authorsList = new ArrayList<>();
      String forDisplayString =
        "Author:" +
        authorsJsonArray.getString(0) +
        "\n" +
        "Quote:" +
        "\n" +
        authorsJsonArray.getString(1)+"\n";



      displayResponse(forDisplayString);

    } else {
      System.out.println("the else for send type caught ");
      System.out.println(receivedObject.startsWith("Authors_JSON:"));
      System.out.println("Received message: " + receivedObject);
      displayResponse(response);
    }
  }

  private void sendAuthor() {
    String selectedAuthor = (String) authorsComboBox.getSelectedItem();
    sender.println("Author_Request:" + selectedAuthor);
    showSuccessMessage();
    String response = receiver.nextLine();
    String receivedObject = response;
    if (receivedObject.startsWith("Quotes_Sent:")) {
      receivedObject = receivedObject.replace("Quotes_Sent:", "");
      JSONArray authorsJsonArray = JsonIO.readArray(receivedObject);
      ArrayList<String> authorsList = new ArrayList<>();
      String forDisplayString =
        "Author:" +
        authorsJsonArray.getString(0) +
        "\n" +
        "Quote:" +
        "\n" +
        authorsJsonArray.getString(1)+"\n";


      displayResponse(forDisplayString);
      
    } else {
      System.out.println("Received message: " + receivedObject);
      displayResponse(response);
    }
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

  public static void main(String[] args) throws FileNotFoundException {
    String icon1_path = "src/client/forutune_icon.png";
    String icon2_path = "src/client/forutune_icon2.png";
    ImageIcon icon1 = new ImageIcon(icon1_path);
    Image icon1Image = icon1.getImage();
    ImageIcon icon2 = new ImageIcon(icon2_path);
    String configFilePath = "src/client/config.json";
    File configFile = new File(configFilePath);
    JSONObject configJsonObject = JsonIO.readObject(configFile);
    @SuppressWarnings("deprecation")
    Double port = new Double(configJsonObject.get("server-port").toString());
    int portint = port.intValue();
    System.out.println("Config port:" + Integer.toString(portint));
    String server_address = configJsonObject.get("server-address").toString();
    System.out.println("Config Adress:" + server_address);
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          List<Image> icons = new ArrayList<Image>();
          FortuneClient client = new FortuneClient();
          client.setIconImages(icons);
          client.setIconImage(icon1Image);
          client.setVisible(true);
          client.connectToServer(portint, server_address);
        }
      }
    );
  }

  private void connectToServer(int portint, String server_address) {
    try {
      socket = new Socket(server_address, portint);
      receiver = new Scanner(socket.getInputStream());
      sender = new PrintWriter(socket.getOutputStream(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
