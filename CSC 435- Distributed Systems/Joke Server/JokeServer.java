/*--------------------------------------------------------
1. Omar Chughtai / 1/14/2018:

2. Java Version 1.8.0_161

3. Compilation instructions
javac JokeServer.java

4. Instructions to run program
In separate shell windows run the following:
> java JokeServer


All acceptable commands are displayed on the various consoles.

This program can run across machines, so if using client on different machine, must pass in server
ip address as command line argument.

Example:
> java JokeServer            - runs on default port, 4545
> java JokeServer secondary  - runs primary and secondary servers - primary on 4545 and secondary on 4546

5.Files needed to run program 
 a. JokeServer.java
 b. JokeClient.java - optional to request joke/proverb.
 c. JokeClientAdmin.java - optional to switch from proverb/joke to joke/proverb.

6. Notes:
JokeLog file created in same directory as where class file is located. Can change name if needed.
----------------------------------------------------------*/
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

enum SERVERMODE { JOKE, PROVERB}
	

//Worker thread responsible for handling client requests, returns a joke or proverb based on server mode.
class Worker extends Thread 
{
	Socket sock;
	ModeServer modeServer;	
	String[] jokeProverbHeader = {"A","B","C","D"};
	private boolean isPrimary;
	private HashMap<String,ClientState>  usersState=new HashMap<String,ClientState>();	
	
	Worker(ModeServer modeServer,Socket sock, boolean isPrimary, HashMap<String,ClientState> usersState)
	{
		//socket used to communicate with client
		this.sock = sock;
		//mode server used to switch modes.  used to get current operating mode(proverb or joke)
		this.modeServer = modeServer;		
		//hash map that stores uuid uniquely representing a user along with ClientState object that constains a user's state.
		this.usersState = usersState;
		//used to determine is server running the primary or secondary server.
		this.isPrimary = isPrimary;
	}
	
	//method responsible for doing work, finds ip address based on domain name given.
	public void run()
	{
		PrintStream out = null;
		BufferedReader in = null;
		try
		{			
			ClientState cs;			
			//Creates input using the socket's input stream wrapped with InputStreamReader and BufferedReader
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String clientMessage = in.readLine();
			//parse message, split into two parts, uuid and name.
			int messageIndex = clientMessage.indexOf(" ");
			String uuid = clientMessage.substring(0,messageIndex);
			String name = clientMessage.substring(messageIndex);		
			//if usersState hashmap already contains uuid, then return client state for that uuid.
			if (usersState.containsKey(uuid))
			{
				cs = usersState.get(uuid);
			}
			//no entry found, create new client state and associate with given uuid.
			else
			{
				cs = new ClientState(JokeServer.getJokes(),JokeServer.getProverbs()); 
				usersState.put(uuid,cs);							
			}
			
			//output set to socket output stream, wraps with PrintStream class.		
			out = new PrintStream(sock.getOutputStream());
			
			//if mode server is in joke mode, return a joke.
			if (modeServer.getMode() == SERVERMODE.JOKE)
			{
				System.out.println("Client has connected, now returning a joke.");		
				JokeLogger.getInstance().logMessage("Client has connected, now returning a joke.");								
				//gets a random joke, reset if all jokes(4 total) have been returned.
				String joke = cs.getRandomJoke();			
				//gets index of joke.
				int jokeIndex = JokeServer.getJokes().indexOf(joke);
				String jokeMessage;
				//if primary server
				if (isPrimary)
				{					
					jokeMessage = String.format("J%s %s: %s",jokeProverbHeader[jokeIndex],name,joke);					
				}
				//secondary server, add <S2> header before rest of string.
				else
				{
					jokeMessage = String.format("<S2> J%s %s: %s",jokeProverbHeader[jokeIndex],name,joke);
				}
				out.println(jokeMessage);				
				JokeLogger.getInstance().logMessage(jokeMessage + "\n");
			}
			//if mode server is in proverb mode, return a proverb.
			else if ( modeServer.getMode() == SERVERMODE.PROVERB)
			{
				System.out.println("Client has connected, now returning a proverb.");		
				JokeLogger.getInstance().logMessage("Client has connected, now returning a proverb.");								
				//gets a random proverb, reset if all proverbs(4 total) have been returned.
				String proverb = cs.getRandomProverb();
				//gets index of proverb.
				int proverbIndex = JokeServer.getProverbs().indexOf(proverb);			
				String proverbMessage;
				//if primary server
				if (isPrimary)
				{
					proverbMessage = String.format("P%s %s: %s",jokeProverbHeader[proverbIndex],name,proverb);				
				}
				//secondary server, add <S2> header before rest of string.
				else 
				{				
					proverbMessage = String.format("<S2> P%s %s: %s",jokeProverbHeader[proverbIndex],name,proverb);			
				}
				out.println(proverbMessage);
				JokeLogger.getInstance().logMessage(proverbMessage + "\n");
				
			}
			//close socket once finished with work.
			sock.close();
		}
		catch (IOException ex)
		{
			//Any IOException will get caught here.
			System.out.println(ex);
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			JokeLogger.getInstance().logMessage(sw.toString());
			JokeLogger.getInstance().logMessage(ex.getMessage());
		}		
	}	
		
}

//Class represents state of a user client, keeps track of jokes and proverbs being returned and resets the jokes/proverbs once all
//have been returned to the client.
class ClientState
{
	private int jokeCount = 0;
	private int proverbCount = 0;
	private HashMap<String,Boolean> seenJokeList;
	private HashMap<String,Boolean> seenProverbList;
	
	public ClientState(ArrayList<String> jokes,ArrayList<String> proverbs)
	{		
		//hashmap has all jokes along with boolean value indicating whether joke has been returned to client or not.		
		seenJokeList = new HashMap<String,Boolean>();
		//hashmap has all proverbs along with boolean value indicating whether proverb has been returned to client or not.
		seenProverbList = new HashMap<String,Boolean>();
		//initially all jokes and proverbs have not been returned, set all values in each hashmap to false.
		for (String joke : jokes)
		{
			seenJokeList.put(joke,false);
		}
		for (String joke : proverbs)
		{
			seenProverbList.put(joke,false);
		}
		
	}	
	
	//Gets a random joke, if every joke has been returned, then resets and starts again.
	public String getRandomJoke()
	{			
		ArrayList<String> jokes = new ArrayList<String>();
		for(Map.Entry<String,Boolean> entry : seenJokeList.entrySet())
		{
			//only adds if joke has not been seen yet.
			if (entry.getValue() == false) 
				jokes.add(entry.getKey());
		}
		//creates a random object and gets a random index from arraylist containing all jokes that haven't been returned yet.
		Random rand = new Random();
		int index = rand.nextInt(jokes.size());		
		//gets the specified joke, and puts it in the seen joke hashmap.
		String joke = jokes.get(index);		
		seenJokeList.put(joke,true);
		jokeCount++;		
		//checks if all jokes have been returned, if so reset count and hashmap of seen jokes.
		if (jokeCount == 4)
		{
			jokeCount = 0;
			resetJokes();
		}
		return joke;		
	}
	
	//Gets a random proverb, if every proverb has been returned, then resets and starts again.
	public String getRandomProverb()
	{
		ArrayList<String> proverbs = new ArrayList<String>();
		for(Map.Entry<String,Boolean> entry : seenProverbList.entrySet())
		{
			//only adds if proverb has not been seen yet.
			if (entry.getValue() == false)
				proverbs.add(entry.getKey());
		}
		//creates a random object and gets a random index from arraylist containing all proverbs that haven't been returned yet.
		Random rand = new Random();
		int index = rand.nextInt(proverbs.size());		
		//gets the specified proverb, and puts it in the seen proverb hashmap.
		String proverb = proverbs.get(index);
		seenProverbList.put(proverb,true);
		proverbCount++;
		//checks if all proverbs have been returned, if so reset count and hashmap of seen proverbs.
		if (proverbCount == 4)
		{
			proverbCount = 0;
			resetProverbs();
		}
		return proverb;		
		
	}
	
	//sets all proverbs in seen hash map list to false to start over again
	private void resetProverbs()
	{
		for(Map.Entry<String,Boolean> entry : seenProverbList.entrySet())
		{
			entry.setValue(false);
		}
		
	}
	
	//sets all jokes in seen hash map list to false to start over again
	private void resetJokes()
	{
		for(Map.Entry<String,Boolean> entry : seenJokeList.entrySet())
		{
				entry.setValue(false);
		}		
	}
}

//Class represents a worker thread for the mode server. 
class ModeWorker extends Thread 
{
	
	private Socket socket;
	PrintStream out = null;
	private ModeServer modeServer;	
	
	public ModeWorker(Socket s,ModeServer ms)
	{
		//Gets socket object used to communicate with client.
		socket = s;
		//specified ModeServer object
		modeServer= ms;		
	}
	
	public void run()
	{						
		try
		{
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintStream(socket.getOutputStream());		
		String command;	
		//gets input from client.
			command = in.readLine();
			
				switch (command)
				{
					//client pressed 'enter', switch server modes now. Notify client of new server mode now.
					case "":
						if (modeServer.getMode() == SERVERMODE.PROVERB)
						{
							System.out.println("Switching to joke mode");						
							modeServer.setMode(SERVERMODE.JOKE);							
							out.println("Server is now in Joke mode.");
							JokeLogger.getInstance().logMessage("Server is now in Joke mode.\n");
						}
						else if (modeServer.getMode() == SERVERMODE.JOKE)
						{
							System.out.println("Switching to proverb mode");						
							modeServer.setMode(SERVERMODE.PROVERB);							
							out.println("Server is now in Proverb mode.");
							JokeLogger.getInstance().logMessage("Server is now in Proverb mode.\n");
						}
						break;										
					//invalid command entered, notify client
					default:
						System.out.println("Invalid command entered.");
						JokeLogger.getInstance().logMessage("Invalid command entered.\n");
						out.println("Invalid command entered, please hit 'enter' to switch modes, or 'quit' to exit.");				
						break;					
						
				}					
		}
		
		
		catch (IOException ex)
		{
			//Any IOException will get caught here.
			System.out.println(ex);
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			JokeLogger.getInstance().logMessage(sw.toString());
			JokeLogger.getInstance().logMessage(ex.getMessage());
		}	
	}
}

//Class represents a thread responsible for maintaining the mode of the Joke Server.
class ModeServer extends Thread
{
	SERVERMODE currentMode;
	private int port;
	private int qlens;
	private boolean serverIsRunning;
	private ServerSocket modeServer;		

	public ModeServer(int port, int qlens)
	{
		//specified admin port to listen on.
		this.port = port;
		//allowable client connections allowed in queue.
		this.qlens = qlens;		
		//The main joke server object.		
	}
	public void run()
	{		
		try
		{
		serverIsRunning = true;
		//create ServerSocket object on specified admin port.
		modeServer = new ServerSocket(port,qlens);
		//current mode set initially to joke mode.
		currentMode = SERVERMODE.JOKE;
		Socket sock;
		System.out.println("Started admin server on separate thread.");
		System.out.println("Current mode: " + currentMode);
		JokeLogger.getInstance().logMessage("Started admin server on separate thread.");
		JokeLogger.getInstance().logMessage("Current mode: " + currentMode);
		//loop continously waiting for client connections, and create a new mode worker thread for each client connection.
		while (serverIsRunning)
		{
			
			sock = modeServer.accept();
			new ModeWorker(sock,this).start();	
		}
				
		}
		catch(Exception ex)
		{			
			System.out.println(ex.getMessage());
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			JokeLogger.getInstance().logMessage(sw.toString());
			JokeLogger.getInstance().logMessage(ex.getMessage());
		}
	}	
	
	
	//sets the current server mode.
	public void setMode(SERVERMODE mode)
	{		
		currentMode = mode;			
	}
	
	//gets the current server mode.
	public SERVERMODE getMode()
	{
		return currentMode;
	}
	
}

//Initial starting point for Joke Server program, starts primary server and secondary server if argument "secondary" given.
public class JokeServer {
	
	private static final int  ADMIN_PRIMARY_PORT = 5050;
	private static final int ADMIN_SECONDARY_PORT = 5051;
	private static final int Q_LEN = 6;
	private static final int PRIMARY_SERVER_PORT = 4545;
	private static final int SECONDARY_SERVER_PORT = 4546;
	
	
	public static void main(String args[]) throws IOException
	{	
		//Set JokeLogger file name to specific file used to log messages from JokeServer.
		JokeLogger.getInstance().setFileName("JokeLog.txt");	
		boolean serverIsRunning = true;
		ServerSocket servsock = null;
		Socket sock = null;
		int serverPort=PRIMARY_SERVER_PORT;
		int adminServerPort=ADMIN_PRIMARY_PORT;
		boolean isPrimary = true;
		//hashmap structure used to keep track of the state of each client using uuid.
		HashMap<String,ClientState> usersState = new HashMap<String,ClientState>();
		//if one commandline argument given, check for 'secondary' value
		if (args.length == 1)
		{
			//if 'secondary', set server and admin server port to correct values.
			if (args[0].toLowerCase().equals("secondary"))
			{
				serverPort = SECONDARY_SERVER_PORT;
				adminServerPort = ADMIN_SECONDARY_PORT;	
				isPrimary = false;
			}
			else
			{
				System.out.println("Invalid commandline argument given");
				System.exit(1);
			}
			
		}
		//use values for primary server.
		else
		{
			serverPort = PRIMARY_SERVER_PORT;
			adminServerPort = ADMIN_PRIMARY_PORT;
			isPrimary = true;
		}
		
		//create instance of modeserver to run in its own thread, using adminServerPort.
		ModeServer ms = new ModeServer(adminServerPort,Q_LEN);
		ms.start();
		
		try
		{
		//create socket on server used to listen for incoming requests from client.
		 servsock = new ServerSocket(serverPort,Q_LEN);
		
		System.out.println("Omar Chughtai's Joke server 1.8 starting up, listening at port " + serverPort + ".\n");						
		JokeLogger.getInstance().logMessage("Omar Chughtai's Joke server 1.8 starting up, listening at port " + serverPort + ".\n");
		
		//keeps looping and waits for a client connection in which each client connection is handled by worker thread to get the response.
		while (serverIsRunning)
		{
		
			//blocks until client connects, assigns sock object to handle specific client request.
			sock = servsock.accept();
			//creates new thread to work on request.
			new Worker(ms,sock,isPrimary,usersState).start();					
		}			
			servsock.close();	
		}
		catch(IOException ex)
		{
			System.out.println(ex);
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			JokeLogger.getInstance().logMessage(sw.toString());
			JokeLogger.getInstance().logMessage(ex.getMessage());
		}					
	}		
	
	//returns the jokes to be returned to a client.
	public static ArrayList<String> getJokes()
	{
		ArrayList<String> jokes = new ArrayList<String>();
		jokes.add("What's the object oriented way to become wealthy? Inheritance.");
		jokes.add("Atoms are untrustworthy little critters. They make up everything!");
		jokes.add("The past, the present, and the future walk into a bar. It was tense.");
		jokes.add("0 is false and 1 is true, right? 1.");		
		
		return jokes;		
	}
	
	//returns the proverbs to be returned to a client.
	public static ArrayList<String> getProverbs()
	{	
		ArrayList<String> proverbs = new ArrayList<String>();
		proverbs.add("Live and learn");
		proverbs.add("Actions speak louder than words.");
		proverbs.add("Attack is the best means of defense");
		proverbs.add("Lying is a disease, and truth is a cure.");
		
		return proverbs;		
	}
			
}

//Class is responsible for logging messages, singleton class only created once.
class JokeLogger
{	
	private static JokeLogger instance= new JokeLogger();
	private PrintWriter out;
	private String fileName;
	
	
	private JokeLogger()
	{
		
	}
	
	//returns the single instance.
	public static JokeLogger getInstance()
	{		
		return instance;
	}
	
	//sets the filename used to be written to.
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
		
	}	
	
	//logs a given message to specified file, synchronized to prevent threads from interfering with each other.
	public synchronized void logMessage(String message) 
	{				
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true))); 
			out.println(message);	
			out.flush();
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
		}						
		finally
		{
			out.close();
		}				
	}
	
	
	
}