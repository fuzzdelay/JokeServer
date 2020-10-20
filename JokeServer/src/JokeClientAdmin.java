
/*--------------------------------------------------------

1. Name / Date: Jonathan Hense / 13 January 2020

2. Java version used, if not the official version for the class: 1.8

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:



In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeAdminClient

Once each file has been compiled and is run in separate windows, either:
a) from the client window, enter name and continue  pressing enter to hear joke or proverb
b) from the clientAdmin window, press enter to switch the mode from joke to proverb
--note that the server window is not interactive other than using some sort of kill command (CTRL-X-C for example)

5. List of files needed for running the program.


 a. JokeServer.java
 b. JokeClient.java
 c. JokeAdminClient.java

5. Notes:

My server is rather heavy or "thick", as Dr. Elliott presented the term in class, while my client 
is fairly lightweight or "thin". I coded this way because 
it's simply what made the most sense to me as I was plotting out classes and sequence. 
I wonder about scalability here and whether I ought to have placed just a bit more 
on the proverbial shoulders of the client. 
You'll find that all Workers (thread generators) and methods for populating, selecting, 
and sending jokes/proverbs will be found here. 
I'm still rather "green" at object oriented programming and this is hands down the 
largest program I've written in terms of lines of code;
It could be prettier, but it works.




----------------------------------------------------------*/
import java.io.*;
import java.net.*;


public class JokeClientAdmin {
	static int port;//global static port for the JokeClientAdmin
	static int portOne = 5050;
	static int portTwo = 5051;

	public static void main(String [] args) {
		String serverName;
		//String secondaryServerName;
		if(args.length < 1) {
			serverName = "localhost"; //pass localhost to serverName if no args
			port = portOne;
		}
		/*if(args.length>1 && args[0]=="secondary") {
			port = portTwo;
			serverName = args[1];
		}*/
		else {
			serverName = args[0]; //otherwise pass to serverName the IP address provided
			port = portOne;
		}
		
		
		System.out.println("Jonathan Hense's JokeClientAdmin up and running.\nUsing server "+serverName+" at port "+port);
		//prints to client the serverName and port to which they have connected
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
		//new BufferedReader obj called "in" which takes in InputStreamReader for input
		
		try {
			String entry; //string var for the current mode, which will be joke mode on default
			//String mode;
			do {
				System.out.println("Press <Enter> to toggle between joke and proverb modes");
				System.out.flush();
				//repeatedly prompts ClientAdmin to Press <Enter> to toggle modes
				entry = in.readLine();
				//reads in ClientAdmin entry;
				if (entry.equals("")) {
					//if admin entry is the <Enter> key,
					toggle(entry, serverName);
					//call toggle method
				}
			} while(entry.indexOf("quit")<0);
			//loop continues until ClientAdmin types and enters "quit"
			System.out.println("Cancelled by user request");
			//if client types and enters "quit", the loop ends and we print this message
		}catch(IOException ioe) {
			ioe.printStackTrace();
		
	}
}

public static void toggle(String mode, String serverName) {
	//method creates socket for communication, input/output devices, and then sends the mode to ModeServer
	Socket adminSocket;
	//var for our socket
	BufferedReader fromServer;
	//instantiated reader object for input
	PrintStream toServer;
	//instantiated stream object for output
	String textFromServer;
	//strin var to hold text read in from server
	
	try {
		adminSocket = new Socket(serverName, port);
		//create new Socket with designated serverName and port for communication
		fromServer = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
		//set up input var
		toServer = new PrintStream(adminSocket.getOutputStream());
		//set up output var
		toServer.println(mode);
		toServer.flush();
		//sends the mode to the server, which will be read in and processed according to server's toggle method
		//which will change from joke mode to server mode or vice versa

		for (int i = 1; i <= 3; i++){
			//reads in text from server
			textFromServer = fromServer.readLine();
			//stores as input the output from server
			if (textFromServer != null)
				System.out.println(textFromServer);
			//as long as output from server is not null, we'll print the text
		}
	}catch(IOException ioe) {
		System.out.println("Socket error");
		ioe.printStackTrace();
	}
}
}

