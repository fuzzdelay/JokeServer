/*--------------------------------------------------------

1. Name / Date: Jonathan Hense / 13 January 2020

2. Java version used, if not the official version for the class: 1.8

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClient.java


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
is fairly lightweight or "thin". Minimal cookie on the client here. I coded this way because 
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
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;



public class JokeClient {
	static boolean secondaryServerMode = false;
	static int port;
	static int portOne = 4545;
	static int portTwo = 4546;
	static String userName;
	static Boolean newUser = true; // boolean var to check whether user is new or if they have an existing userName and UUID
	static String uuid = UUID.randomUUID().toString(); //client generates their own UUID which will be sent to server to assist in maintaining state
	static Socket socket;
	
	public static void main (String [] args) throws IOException {
		String serverName = null;
		//String secondaryServerName = null;
		if(args.length<1) {
			serverName = "localhost";
			port = portOne;
			System.out.println("Jonathan Hense's JokeClient v1.8");
			System.out.println("Connected to server: " + serverName + ", Port: "+ port +"\n");
		}
		/*if(args.length>1 && args[0] =="secondary") {
			secondaryServerName = args[1];
			port = portTwo;
		}*/
		else {
			serverName = args[0]; // default to naming as localhost unless additional arguments provided to command line
			port = portOne;
			System.out.println("Jonathan Hense's JokeClient v1.8");
			System.out.println("Connected to server: " + serverName + ", Port: "+ port +"\n");
		}
		
		/*
		if(args.length>1) { System.out.println("Server one: "+serverName);
		System.out.println("Server two: "+ secondaryServerName);
		}*/
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		//PrintStream out; 

		String entry;
		//var for storing client/user <Enter> key entry
		try {
			
			System.out.print("Please type your name and press <enter>. You may quit at any time by typing <quit>\n");
			System.out.flush();
			userName = in.readLine();
			//stores userName, which will be used to greet the user and sent to the server to retrieve upcoming
			//jokes and proverbs

			if(userName.indexOf("quit")<0) {
				//as long as the user doesn't type "quit"
				System.out.println("Welcome "+userName+"! Get Ready for humor/wisdom!\n");
					do {
						//user is prompted to press <Enter> to request and receive a joke or proverb
						System.out.println("Press <Enter> to hear a joke or proverb");
						entry = in.readLine();
						if(entry.equals("")) {
							//System.out.println("testing getJokeOrProverb");
							getJokeOrProverb(userName, serverName);
							//calls getJokeOrProverb method using user's name and server
						}
					}while(entry.indexOf("quit")<0);
					//continue prompting user/client to press <Enter> until the client enters "quit"
			}
		}catch(IOException ioe) {
				ioe.printStackTrace();
	}
	}
	
	static void getJokeOrProverb(String userName, String serverName) {
		//method sends client userName and serverName through the socket it creates in order to
		//receive a response in the form of a joke or proverb
		Socket socket;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		//create input and output objects from/to server (above) and 
		//then pass reader and print stream functionality to communicate
		//via socket
		
		try {
			socket = new Socket(serverName, port);
			//creates new socket to connect with server for the purpose of receiving joke or proverb
			
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toServer = new PrintStream(socket.getOutputStream());
			
			toServer.println(userName); //send user name to server
			toServer.println(uuid); //send the uuid to server
			toServer.flush();//rinse and repeat
			
			for(int i = 0; i<=3; i++) {
				//loop reads lines of text from the server
				textFromServer = fromServer.readLine();
				//reads in and stores text from server
				if(textFromServer != null) System.out.println(textFromServer);
				//as long as there is text to display, we print it out on the client window
			}
			socket.close();
			//close socket to prevent resource leak
		} catch(IOException ioe2) {
			System.out.println("Socket error.");
			ioe2.printStackTrace();
		}
	}
	}
