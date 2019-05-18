import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ChatFrame extends Frame{
	public ChatFrame(){
		setSize(500, 500);
		setTitle("Chat Frame");
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		add(new ChatPanel(), BorderLayout.CENTER);
		setVisible(true);
	}
	public static void main(String[] args){
		new ChatFrame();
	}
}

class ChatPanel extends Panel implements ActionListener, Runnable{  //INCOMPLETE!!!
	TextArea ta;
	TextField tf;
	Button connect, disconnect;
	Thread thread;
	Socket s;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	String username;
	boolean connected;
	java.awt.List userList;

	public ChatPanel(){
			setLayout(new BorderLayout());
			tf = new TextField();
			tf.addActionListener(this);
			ta = new TextArea();
			add(tf, BorderLayout.NORTH);
			Panel p1 = new Panel();
			p1.setLayout(new BorderLayout());
			p1.add(ta, BorderLayout.CENTER);
			userList = new java.awt.List();
			p1.add(userList, BorderLayout.WEST);
			add(p1, BorderLayout.CENTER);
			connect = new Button("Connect");
			connect.addActionListener(this);
			disconnect = new Button("Disconnect");
			disconnect.setEnabled(false);
			disconnect.addActionListener(this);
			Panel p2 = new Panel();
			p2.add(connect);
			p2.add(disconnect);
			add(p2, BorderLayout.SOUTH);
	}
	public void actionPerformed(ActionEvent ae){
		if(ae.getSource() == connect){
			if(!connected){
				try{
					// Get user name from text field
					username = tf.getText();
					if(username == null || username.isEmpty())
					{
						// Verify that the username is valid
						System.out.println("Please enter a valid username");
						return;
					}
					s = new Socket("127.0.0.1", 3000);
					oos = new ObjectOutputStream(s.getOutputStream());
					ois = new ObjectInputStream(s.getInputStream());
					thread = new Thread(this);
					thread.start();
					connected = true;
					connect.setEnabled(false);
					disconnect.setEnabled(true);
					System.out.println("Connected!!!");
					// Get parent of this panel, and set its title bar text to username
					((Frame)this.getParent()).setTitle(username);
					tf.setText("");
					// Send out a message stating new user entry
					DataObject dobj = new DataObject(username);
					dobj.setMessage("");
					oos.writeObject(dobj);
				}catch(UnknownHostException uhe){
					System.out.println(uhe.getMessage());
				}catch(IOException ioe){
					System.out.println(ioe.getMessage());
				}
			}
		}else if(ae.getSource() == disconnect){
			if(connected){				
				try{
					// Send out a message stating current user's exit
					DataObject dobj = new DataObject(username);
					dobj.setMessage("BYE");
					oos.writeObject(dobj);
					// UI changes
					tf.setText("");
					connected = false;
					connect.setEnabled(true);
					disconnect.setEnabled(false);
					userList.removeAll();

					// Safely closing streams and sockets
					oos.close();
					ois.close();
					s.close();
					
				}catch(IOException ioe){
					System.out.println(ioe.getMessage());
				}		
			}
		}else if(ae.getSource() == tf){
			if(connected){
				try{
					String temp = tf.getText().trim();
					// Create data object to send
					DataObject d1 = new DataObject(username);
					if(userList.getSelectedIndex() != -1) {
						// Private message in case an item is selected in the list
						d1.setPrivateDest(userList.getSelectedItem());

						// Printing private message in text area because it will not be sent back by the server
						// as we are sending it only to the private username that is selected
						ta.append("<Private message to " + userList.getSelectedItem() + ">\n");
						ta.append(username + ": " + temp + "\n");

						// Deselecting selected user because list item cannot be deselected on click and user won't be
						// able to send public messages. Therefore deselecting it here by default.
						userList.deselect(userList.getSelectedIndex());
					}
					// Setting message content
					d1.setMessage(temp);
					oos.writeObject(d1);

					// Update text field
					tf.setText("");
					if(temp.equals("BYE")) {
						// Same as disconnect, update UI
						connected = false;
						connect.setEnabled(true);
						disconnect.setEnabled(false);
						userList.removeAll();
						// Safely closing streams and socket
						oos.close();
						ois.close();
						s.close();
					}
				}catch(IOException ioe){
					System.out.println(ioe.getMessage());
				}		
			}
		}
	}
	public void run(){
		while(connected){
			try{
				DataObject d2 = (DataObject)ois.readObject();
				if(d2.getIsEntry()) {
					// Parsing entry state message and announcing entry
					ta.append("<"+d2.getUsername()+"> has joined the room\n");
					// Updating user list
					userList.add(d2.getUsername());
					continue;
				}
				if(d2.getIsExit()) {
					// Parsing exit state message and announcing exit
					ta.append("<"+d2.getUsername()+"> has left the room\n");
					// Updating user list
					userList.remove(d2.getUsername());
					continue;
				}
				if(!d2.getPrivateDest().isEmpty()) {
					// Parsing private message state and showing metadata
					ta.append("<Private message from " + d2.getUsername() + ">\n");
				}

				// Printing original message
				String temp = d2.getMessage(), msgFrom = d2.getUsername();
				ta.append(msgFrom + ": " + temp + "\n");


			}catch(IOException ioe){
				System.out.println(ioe.getMessage());
			}catch(ClassNotFoundException cnfe){
				System.out.println(cnfe.getMessage());
			}
		}
	}
}












