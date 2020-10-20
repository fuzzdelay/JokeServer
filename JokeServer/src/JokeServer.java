/*--------------------------------------------------------

1. Name / Date: Jonathan Hense / 13 January 2020

2. Java version used, if not the official version for the class: 1.8

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java


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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

	
public class JokeServer{
	static boolean secondaryServerMode = false;
	//boolean value to indicate whether or not we're in secondary server mode
	public static HashMap<String, ClientData> clientDB = new HashMap<String, ClientData>();
	//this class level client database HasHMap holds user's UUID and instance of class ClientData, which will hold two lists:
	//one for jokes and the other for proverbs that the client has seen thus far

	public static void main(String [] args) throws IOException{
		int q_len = 7; //arbitrary length of backlog
		int port;
		int portOne = 4545; //port for the serverSocket
		//int portTwo = 4546; //secondary port
		Socket socket; //new socket instance to communicate with client
		
		ModeServer modeServer = new ModeServer();
		//create a new instance of the modeServer to handle switches to/from joke/proverb mode
		ServerSocket serverSocket;
		new Thread(modeServer).start();
		//create a new thread which receives the instance of ModeServer;
		try {
			//if(secondaryServerMode == false) {
				port = portOne;
				serverSocket = new ServerSocket(port, q_len);
				System.out.println("Jonathan Hense's JokeServer up and running. Listening at port "+port);
				//create new instance ServerSocket via constructor, tethered to specific port, for listening
				while(true) {
					socket = serverSocket.accept();
					//pass to socket a serverSocket which continually listens for connection
					new Worker(socket).start();
					//create new Worker threads as needed for connecting users
				}	
			/*}else {
				port = portTwo;
					serverSocket = new ServerSocket(port, q_len);
					while(true) {
						socket = serverSocket.accept();
						//pass to socket a serverSocket which continually listens for connection
						new Worker(socket).start();
						//create new Worker threads as needed for connecting users
					}
				}*/
		
			} catch(IOException ioe) {
				System.out.println("Server Error.");
				ioe.printStackTrace();

		}
	}
}

class ClientData{
	//class of methods to operate on client data to maintain the state of jokes and proverbs
	ArrayList<String> clientJokes; //empty arrayList to store jokes that client has heard so they won't be repeated until the list has reached the last joke
	ArrayList<String> clientProverbs;// empty arrayList to store proverbs that the client has heard so they won't be repeated until end of list
	
	public ClientData() {
		//constructor contains 2 arrayLists for the purpose of storing jokesHeard and proverbsHeard thus far
		clientJokes = new ArrayList<String>();//arrayList of jokes that client has heard/seen already
		clientProverbs = new ArrayList<String>();//arrayList of proverbs that the client has heard/seen already
	}
	
	public void updateJokeList(ArrayList<String> jokesHeard) {
		clientJokes = jokesHeard; //update list of jokes heard by client thus far
	}
	
	public void updateProverbList(ArrayList<String> proverbsHeard) {
		clientProverbs = proverbsHeard; //updated list of proverbs heard by client thus far
	}
	
	public ArrayList<String> getJokes(){
		return clientJokes; //method to return the list of list of jokes heard by client thus far
	}
	
	public ArrayList<String> getProverbs(){
		return clientProverbs; //method to return the list of proverbs heard by client thus far
	}
} 
	
	class Worker extends Thread{
		//worker class which will be generated for each client connection with our server

		Socket socket; //socket instance for communication with client
		static String userName; //string var to store client's name, which we'll use for a number of things, including 
		//personalizing the jokes and proverbs, and storing in our client database HashMap
		public static HashMap<String, ClientData> clientDB;// local to Worker, HashMap to store clientName and client data
		static String userID; //string var to store client's uuid
		String mode; //String var to store the mode, which will be "joke" or "proverb"
		static ArrayList<String> jokes = new ArrayList<String>();
		//arrayList for jokes
		static ArrayList<String> proverbs = new ArrayList<String>();
		//arrayList for proverbs
		Worker(Socket s){ //Constructor: Worker inherits from Thread, which implements Runnable
			socket = s; //Pass Socket parameter s to socket, a local var in Worker
		}
		//Worker enables communication via socket between client and server
		
		public void run() {
			PrintStream out = null; //server sends data to client
			BufferedReader in = null; //server takes input from client
			//pass to var mode the current mode from Modeworker, which will determine whether
			//we send a joke or proverb. 
			
			clientDB = JokeServer.clientDB;
			//pass client data from Worker outside its scope to the class level var
	
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//instantiate input for socket
				out =  new PrintStream(socket.getOutputStream());
				//instantiate output for socket

				try {
				
					userName = in.readLine();
					//read in userName from client
					
					userID = in.readLine();
					mode = ModeWorker.mode;

					//read in the uuid from client and store in userID
					//if(JokeServer.secondaryServerMode == false) {
					System.out.println("User "+userName+" is waiting for a joke or proverb");
					//}
					//else {System.out.println("<S2> "+"User "+userName+" is waiting for a joke or proverb");
					//}
					if(!clientDB.containsKey(userID)) {
						//if this is a new client, then
						ClientData newUser = new ClientData();
						//create a new instance of ClientData
						clientDB.put(userID, newUser);
						//and add the user's ID to newUser for tracking which jokes/proverbs have been seen
					}
					jokes = clientDB.get(userID).getJokes();
					//update joke map to the state of individual client
					proverbs = clientDB.get(userID).getProverbs();
					//update the proverbs map to the state of the individual client
					//System.out.println("User "+userName+", UUID: "+userID+" has requested a joke");
					
					sendJokeOrProverbRequest(mode, out);
					//send joke or proverb to client according to the mode via output
				} catch(IOException ioe1) {
					System.out.println("Server read error");
					ioe1.printStackTrace();
				}
				socket.close(); //close connection to prevent resource leak
			} catch(IOException ioe2) {
				System.out.println(ioe2);
			}
		}
		
		static void populateJokes() {
			//adds jokes and the client's userName to the list jokes
			//if(JokeServer.secondaryServerMode == false) {
			jokes.add("JA "+ userName + ": I would tell you a UDP joke, but you might not get it...\n");
			jokes.add("JB "+ userName + ": I started a band called 999 Megabytes. We still haven't gotten a gig...\n");
			jokes.add("JC "+ userName + ": Why do programmers always mix up Halloween and Christmas? \\nBecause Oct 31 equals Dec 25.\n");
			jokes.add("JD "+ userName + ": Have you heard the one about TCP? \nHave you heard the one about TCP?\n Have you heard the one about TCP?\n Have you heard the one about TCP?\n");
		/*	}else {
				jokes.add("<S2>" +"JA "+ userName + ": I would tell you a UDP joke, but you might not get it...\n");
				jokes.add("<S2>" +"JB "+ userName + ": I started a band called 999 Megabytes. We still haven't gotten a gig...\n");
				jokes.add("<S2>" +"JC "+ userName + ": Why do programmers always mix up Halloween and Christmas? \\nBecause Oct 31 equals Dec 25.\n");
				jokes.add("<S2>" +"JD "+ userName + ": Have you heard the one about TCP? \nHave you heard the one about TCP?\n Have you heard the one about TCP?\n Have you heard the one about TCP?\n");
				
				
			}*/
		}
		
		static void populateProverbs() {
			//adds proverbs and the client's userName to the list proverbs
			//if(JokeServer.secondaryServerMode == false){			
			proverbs.add("PA "+ userName + ": No man is an island\n");
			proverbs.add("PB "+ userName + ": Fortune favors the bold\n");
			proverbs.add("PC "+ userName + ": The pen is mightier than the sword\n");
			proverbs.add("PD "+ userName + ": Never look a gift horse in the mouth\n");
			/* }else {
				proverbs.add("<S2>" +"PA "+ userName + ": No man is an island\n");
				proverbs.add("<S2>" +"PB "+ userName + ": Fortune favors the bold\n");
				proverbs.add("<S2>" +"PC "+ userName + ": The pen is mightier than the sword\n");
				proverbs.add("<S2>" +"PD "+ userName + ": Never look a gift horse in the mouth\n");	
			}*/
		}
	
		public static String jokeSelector() {
			//method returns a random joke from the list
			//it's scalable in the sense that if the number if jokes increase, our bound on the max int index
			//is the size of the arrayList storing jokes
			int jokeIndex = 0;
			String joke; //String var for storing the joke from a particular index of the joke list
			
			if(jokes.isEmpty()) {
				//if the joke list is empty, add jokes to the list via populateJokes method
				populateJokes();
				//adds jokes to the jokes list
				jokeIndex = ThreadLocalRandom.current().nextInt(0, jokes.size());
				//generates pseudo random number of the index of the joke list
				//a note on ThreadLocalRandom: I tried using java.util.Random in this method and in the proverbSelector method
				//to obtain an int between 0 and 4 (the size of the arrayList), but couldn't recall the formula. 
				//I checked the Oracle docs of Random and found the related subclass ThreadLocalRandom. IMO this is a much more straight-forward
				//if not more elegant way to get a range of random ints within a particular thread. Putting this one in my toolbox
				joke = jokes.get(jokeIndex); //retrieves the random index of joke list and stores the joke at the index in joke var
				jokes.remove(jokeIndex); //then removes that index from the list so it isn't repeated
			}else {
				//otherwise if the joke list is not empty, 
				jokeIndex = ThreadLocalRandom.current().nextInt(0, jokes.size());
				//we generate a random index from the joke list d
				joke = jokes.get(jokeIndex);
				joke = jokes.remove(jokeIndex);
				//remove it from the list, and then return it
			}
			clientDB.get(userID).updateJokeList(jokes);
			//uses the instance of ClientData's methods to update the state of the jokes seen according to uuid
			return joke;
			
		}
		public static String proverbSelector() {
			//method returns a random proverb from the list
			//it's scalable in the sense that if the number if jokes increase, our bound on the max int index
			//is the size of the arrayList storing proverbs
			int proverbIndex = 0;
			String proverb;
			//we'll use ThreadLocal combined with Random to select a random index between index 0 and the bound: 
			//the size of the jokes/proverbs lists. TLR is handy as it contains or isolates the the random objects to the 
			//specific thread
			if(proverbs.isEmpty()) {
				//if the proverbs list is empty, add proverbs to the list via populateProverbs method
				populateProverbs();
				//adds proverbs to the proverbs list
				proverbIndex = ThreadLocalRandom.current().nextInt(0, proverbs.size());
				//generate PRN isolated to the current thread
				proverb = proverbs.get(proverbIndex);
				//pull the index of the proverb list and save in the var to be returned
				proverbs.remove(proverbIndex);
			}else {
				proverbIndex = ThreadLocalRandom.current().nextInt(0, proverbs.size());
				//otherwise generate PRN isolated to the current thread
				proverb = proverbs.get(proverbIndex);
				//stores the proverb
				proverb = proverbs.remove(proverbIndex);
				//and then removes it from the list and returns it
			}
			clientDB.get(userID).updateProverbList(proverbs);
			//uses the instance of ClientData's methods to update the state of the proverbs seen according to uuid
			return proverb;
		}
		
		static void sendJokeOrProverbRequest(String mode, PrintStream out) { 
			//conditional method based off of joke/proverb toggle, where if
			//the mode is set to "proverb", we utilize the proverbSelector method
			//to send proverb to user. otherwise, we do the default, which is 
			//joke mode, which will utilize jokeSelector to send a joke to the client
			if (mode.equals("proverb")) {
				//out.println("testing proverb");
				//if(JokeServer.secondaryServerMode == false) {
				System.out.println("Sending Proverb to "+userName);
				//sends message to server console indicating that proverb has been sent to user
				out.println(proverbSelector());
				//sends randomly selected proverb to user
				//}else {
					//System.out.println("<S2>" +"Sending Proverb to "+userName);

			}else if(mode.equals("joke")){
				//out.println("testing joke");
				//if(JokeServer.secondaryServerMode == false) {
				System.out.println("Sending Joke to "+userName);
				//sends message to server console indicating joke has been sent to user
				out.println(jokeSelector());
				//sends randomly selected joke to user
				//}else {
				//	System.out.println("<S2>" +"Sending Joke to "+userName);
				//}
			}else {
				out.println("Server Error.");
			}
		}
	}
	
		
		class ModeServer implements Runnable{
			//server for client admin to change modes
			ServerSocket serverSocket;
			//instance of serverSocket listening for ClientAdmin to connect and change mode
			public void run() {
				int q_len = 7; //arbitrary queue limit
				int portOne = 5050; //port number for ModeServer communication with JavaClientAdmin
				//int portTwo = 5051;
				Socket socket; //socket instance for connection
				
				try {
					/*boolean secondaryServerMode = JokeServer.secondaryServerMode;
					if(secondaryServerMode  == false) {*/
					serverSocket = new ServerSocket(portOne, q_len);
						while(true) {
							socket = serverSocket.accept();
						//passes listening serversocket to socket
							new ModeWorker(socket).start();
						//generates a new modeworker thread and kicks off
						}
					/*}else {
						serverSocket = new ServerSocket(portTwo, q_len);
						while(true) {
							socket = serverSocket.accept();
							new ModeWorker(socket).start();
						}
					}*/
				}catch(IOException ioe) {
					System.out.println("Server Error.");
					System.out.println(ioe);
				}

			}	
		}
		
		class ModeWorker extends Thread{
			//this thread changes the mode from joke to proverb repeatedly whenever the user presses <enter>
			Socket socket; //socket var
			static Boolean jokeMode = true; //our mode starts in joke mode by default
			static String mode = "joke"; //mode set to joke by default
			static String entry;//var to accept <enter> key press from ClientAdmin
			
			ModeWorker(Socket s){
				//constructor for ModeWorker, passes arg to socket for open line
				socket = s; 
			}
			
			static boolean toggle() {
				//method to toggle between joke mode and proverb mode
				jokeMode = !jokeMode;
				//whatever boolean value jokeMode is set to will be switched upon method call
				return jokeMode;
			}
			
			
			static void updateMode(PrintStream out) {
				//method that works with the toggle method to set String mode to "joke" or "proverb",
				//then prints to server that the mode has been toggled
				if(jokeMode == true) {
					mode = "joke";
					//if(JokeServer.secondaryServerMode ==false) {
					System.out.println("Server is in Joke Mode");
					//sends message to server console that the mode is in joke mode
					out.println(mode);
					//sends mode to ClientAdmin
					//}else {
					//	System.out.println("<S2> "+"Server is in Joke Mode");
					//}
				}else{
					mode = "proverb";
					//if(JokeServer.secondaryServerMode ==false) {
						
					
					System.out.println("Server is in Proverb Mode");
					//sends message to server console that the mode is in joke mode
					out.println(mode);
					//sends mode to ClientAdmin
					//}else {
					//	System.out.println("<S2> "+"Server is in Proverb Mode");
					//}
				}
			} 
			public void run() {
				BufferedReader in = null;
				//declare Buffered reader object called in for input
				PrintStream out = null;
				//declare PrintStream object called out for output
				try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					//BufferedReader gets the InputStreamReader object which gets the input stream of the socket
					out = new PrintStream(socket.getOutputStream());
					//PrintStream gets the output stream of socket
					
					try {
						
							//mode = in.readLine();
							entry = in.readLine();
							if(entry.equals("")) {//if user presses <Enter>...
								toggle(); //switch jokeMode to true or false
								updateMode(out); //updates server/client of the new mode
							}
							/*if(entry.equals("s")) {
								JokeServer.secondaryServerMode = !JokeServer.secondaryServerMode;
							}
						*/
					}catch(IOException ioe) {
						System.out.println(ioe);
					}
					socket.close();
					//close the socket to prevent resource leaks
				} catch(IOException ioe) {
					System.out.println(ioe);
				}
			}
		}
		
		
		



	

	

