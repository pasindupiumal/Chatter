import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    DefaultListModel<String> users = new DefaultListModel<String>();
    JList chatUsers = new JList(users);
    String selectedName = null;
    

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {
    	
        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(new JScrollPane(chatUsers), "South");
        
        frame.pack();
        
        

        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
            	       	
            	//Check whether any specific names has been selected in the list box
            	if(chatUsers.isSelectionEmpty()) {
            		out.println(textField.getText());
                    textField.setText("");
            	}
            	else {
            		//Get the selected clients
            		List selectedUsers = chatUsers.getSelectedValuesList();
            		
            		//Get all the selected users into a single string
            		String users = "SELECTED,";
            		
            		for(int i=0; i < selectedUsers.size(); i++) {
            			users += selectedUsers.get(i) + ",";
            		}
            		
            		/**
            		 * Add the clients name to selected names. So that client can also see the 
            		 * message.
            		 */
            		
            		users += selectedName + ",";
            		
            		System.out.println(users);
            		
            		users +=  textField.getText();
            		out.println(users);
            		textField.setText("");
            		
            		System.out.println(users);
            	}
                
            }
        });
        
        
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
    	
    	//Keep the selected name saved for future use
        selectedName = JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
        return selectedName;
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        
        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
        while (true) {
            String line = in.readLine();
            
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
                System.out.println("Selected name is : " + selectedName);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("LIST,")) {
            	String[] array = line.split(",");
            	array[0] = null;
            	users.removeAllElements();
            	
            	for(String user: array) {
            		users.addElement(user);
            	}
            }
            
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}