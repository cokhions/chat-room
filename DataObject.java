import java.io.*;
import java.util.*;

public class DataObject implements Serializable{

	// The username of the source
	private String username;
	// The message content
	private String message;
	// Target username for private messages
	private String privateDest;
	// Whether this message indicates new user entry
	private boolean isEntry;
	// Whether this message indicates existing user exit
	private boolean isExit;

	DataObject(String username){
		// Instantiation with required username and default values for all other properties
		this.username = username;
		message = "";
		privateDest = "";
		isEntry = false;
		isExit = false;
	}

	// Default getters and setters

	public String getMessage(){
		return message;
	}

	public void setMessage(String inMessage){
		message = inMessage;

		// Using BYE as a command to exit
		if(message.equals("BYE")) {
			isExit = true;
		}
		else if(message.isEmpty()) {
			// Using empty string as a command for entry
			isEntry = true;
		}
	}

	public String getUsername(){
		return username;
	}

	public boolean getIsEntry(){
		return isEntry;
	}

	public void setIsEntry(boolean val){
		isEntry = val;
	}

	public boolean getIsExit(){
		return isExit;
	}

	public void setIsExit(boolean val){
		isExit = val;
	}

	public String getPrivateDest(){
		return privateDest;
	}

	public void setPrivateDest(String pdest){
		privateDest = pdest;
	}
}