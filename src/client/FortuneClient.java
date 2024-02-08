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
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import merrimackutil.json.*;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

public class FortuneClient extends JFrame {

  private JTextField messageField;
  private JTextArea responseArea;
  private JComboBox<String> optionsComboBox;
  private Socket socket;
  private Scanner receiver;
  private PrintWriter sender;

  public FortuneClient() {
    // this.setIconImage(getIconImage("src/client/16.png"));
    setTitle("Fortune Client");
    setSize(400, 550);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // this.setIconImage(getIconImage("src/client/16.png"));

    JPanel panel = new JPanel();

    getContentPane().add(panel);

    panel.setLayout(null);
    // panel.setIconImage(img.getImage());

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

    JButton sendMessageButton = new JButton("Send Echo");
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
    responseArea.setBounds(20, 110, 340, 400);
    responseArea.setLineWrap(true);
    responseArea.setWrapStyleWord(true);
    // JScrollPane sPane=new JScrollPane(responseArea);
    responseArea.setEditable(false);
    panel.add(responseArea);
    // panel.add(sPane);
  }

  private void sendMessage() {
    String message = messageField.getText();
    String selectedOption = (String) optionsComboBox.getSelectedItem();
    if (!message.isEmpty()) {
      sender.println("Message:" + message);
      showSuccessMessage();
      // get response from server and display it
      String response = receiver.nextLine();
      // store the received message as an object
      Object receivedObject = response;
      // println the received message
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

  public static void main(String[] args) throws FileNotFoundException {
    String icon1_path = "src/client/forutune_icon.png";
    String icon2_path = "src/client/forutune_icon2.png";
    ImageIcon icon1 = new ImageIcon(icon1_path);
    Image icon1Image = icon1.getImage();
    ImageIcon icon2 = new ImageIcon(icon2_path);
    // FortuneClient.setIconImage(icon1);
    String configFilePath = "src/client/config.json";
    File configFile = new File(configFilePath);
    JSONObject configJsonObject = JsonIO.readObject(configFile);
    @SuppressWarnings("deprecation")
    Double port = new Double(configJsonObject.get("server-port").toString());
    int portint = port.intValue();
    System.out.println("Config port:" + Integer.toString(portint));
    String server_address = configJsonObject.get("server-address").toString();
    System.out.println("Config Adress:" + server_address);

    //server-address
    ///server-port
    //server-address
    ///server-port

    //start swing
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          List<Image> icons = new ArrayList<Image>();
          // icons.add(new ImageIcon("src/client/16.png").getImage());
          // icons.add(new ImageIcon("src/client/32.png").getImage());
          // JLabel myLabel = new JLabel(new ImageIcon("src/client/16.png"));

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
      // ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
      // System.out.println("object:");
      receiver = new Scanner(socket.getInputStream());
      sender = new PrintWriter(socket.getOutputStream(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
