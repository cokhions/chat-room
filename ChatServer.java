import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer{  
	public static void main(String[] args ){  
		ArrayList<ChatHandler>handlers = new ArrayList<ChatHandler>();
		try{  
			ServerSocket s = new ServerSocket(3000);
			for(;;){
				Socket incoming = s.accept();
				new ChatHandler(incoming, handlers).start();
			}
		}catch (Exception e){  
			System.out.println(e);
		} 
	} 
}	  
	  
class ChatHandler extends Thread{
	DataObject myObject = null;
	private Socket incoming;
	// Username to distinguish each thread and for private messages to identify destination
	String username;
	ArrayList<ChatHandler>handlers;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	public ChatHandler(Socket i, ArrayList<ChatHandler>h){
		incoming = i;
		handlers = h;
		handlers.add(this);
	}
	public void run(){
		try{
			in = new ObjectInputStream(incoming.getInputStream());
			out = new ObjectOutputStream(incoming.getOutputStream());

			boolean done = false;
			while (!done){  
				DataObject objIn = (DataObject)in.readObject();
				if (objIn == null){
					done = true;
				}else{
					// Setting the username from data objet received by this handler
					username = objIn.getUsername();
					for(ChatHandler h : handlers){
						// Parsing private destination if any
						String privateDest = objIn.getPrivateDest();
						if(privateDest.isEmpty() || privateDest.equals(h.username))
						{
							// If public or identified as private destination, write in that stream 
							h.out.writeObject(objIn);
						}
						
						if(objIn.getIsEntry() && h != this) {
							// If parsed as entry message, send all other existing usernames to this client
							// so that it can form its user list
							DataObject dumEntry = new DataObject(h.username);
							dumEntry.setMessage("");
							this.out.writeObject(dumEntry);
						}
					}
					if (objIn.getMessage().trim().equals("BYE")){
						// Identify exit state and mark flag to further close this thread
						done = true;
					}
				}
			}
			incoming.close();
		}catch (Exception e){  
			System.out.println(e);
		}finally{
			handlers.remove(this);
		} 
	} 
}
