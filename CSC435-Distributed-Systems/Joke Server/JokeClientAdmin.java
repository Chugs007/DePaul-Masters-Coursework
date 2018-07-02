/*--------------------------------------------------------
1. Omar Chughtai / 1/14/2018:

2. Java Version 1.8.0_161

3. Compilation instructions
javac JokeClientAdmin.java

4. Instructions to run program
In separate shell windows run the following:
> java JokeServer
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This program can run across machines, so if using client on different machine, must pass in server
ip address as command line argument.

Example:
> java JokeClientAdmin                - connects to host at localhost address
> java JokeClientAdmin 140.192.1.22   - connect to host at given ip
> java JokeClientAdmin localhost - connects to to host at localhost address
> java JokeClientAdmin localhost localhost - connects to host at localhost, and secondaryServer at localhost with the option to switch between the two.
> java JokeClientAdmin localhost 140.192.1.22 - connects to host at localhost, and secondaryServer at given ip address for 2nd argument. Can switch between the two.

5.Files needed to run program 
 a. JokeServer.java
 b. JokeClientAdmin.java
 c. JokeClient.java- optional to request joke/proverb from JokeServer.

6. Notes:
JokeLog file created in same directory as where class file is located. Can change name if needed.
----------------------------------------------------------*/
import java.io.*;
import java.net.*;

//Class represents admin client responsible for connecting to joke server and having the ability to switch modes on the server.
public class JokeClientAdmin
{
	private static final int ADMIN_PRIMARY_PORT = 5050;
	private static final int ADMIN_SECONDARY_PORT = 5051;
	
	public static void main(String args[]) throws IOException
	{
		String serverName;
		String primaryServer="";
		String secondaryServer="";
		JokeLogger.getInstance().setFileName("JokeLog2.txt");
		boolean isPrimaryServer = true;
		boolean secondaryServerExists = false;
		int primaryPort = ADMIN_PRIMARY_PORT;
		int secondaryPort = ADMIN_SECONDARY_PORT;
		int port = primaryPort;
		
		//if 2 arguments given, secondaryServer used
		if (args.length == 2)
		{
			primaryServer = args[0];
			secondaryServer = args[1];
			secondaryServerExists = true;
			
		}				
		else if (args.length == 1)
		{
			primaryServer = args[0];
		}
		else 
			primaryServer = "localhost";
		
		serverName = primaryServer;	
		System.out.println("Omar Chughtai's Joke Admin Client.");
		JokeLogger.getInstance().logMessage("Omar Chughtai's Joke Admin Client.");
		System.out.println("Server one: " + primaryServer + ", Port: " + primaryPort);
		JokeLogger.getInstance().logMessage("Server one: " + primaryServer + ", Port: " + primaryPort);		
		if (secondaryServerExists)
		{
			System.out.println("Server two: " + secondaryServer + ", Port: " + secondaryPort + "\n");		
			JokeLogger.getInstance().logMessage("Server two: " + secondaryServer + ", Port: " + secondaryPort + "\n");
		}		
		System.out.println("Using server: " + serverName + ", Port: " + port + ".");		
		JokeLogger.getInstance().logMessage("Using server: " + serverName + ", Port: " + port + ".");
		
		//Creates input using System.in to read from standard input
		//Wraps with InputStreamReader and BufferedReader classes.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String textFromServer;
		String input="";	
					
		try
		{	
			//start of do while loop
			do
			{		
				System.out.println("Please hit enter to switch modes(Joke/Proverb) or 'quit' to quit program.");
				JokeLogger.getInstance().logMessage("Please hit enter to switch modes(Joke/Proverb) or 'quit' to quit program.");
				//Shows message for switching to secondary server.
				if (args.length == 2)
				{
					System.out.println("Press 'S' to switch to secondary server/back to primary server.\n");
				}
				System.out.flush();
				//gets user input
				input = in.readLine();
				JokeLogger.getInstance().logMessage(input + "\n");
				//if invalid input keep looping
					while((!input.equals("")) && (!input.toLowerCase().equals("quit")) && (!input.toLowerCase().equals("s")))
				{
					System.out.println("Please hit enter to switch modes(Joke/Proverb) or 'quit' to quit program.");
					if (args.length == 2)
					{
						System.out.println("Press 'S' to switch to secondary server/back to primary server.\n");
					}
					input = in.readLine();
				}
				//if 's' input detected
				if (input.toLowerCase().equals("s"))
				{
					//switch servers
					if (args.length == 2)
					{						
						if (isPrimaryServer)
						{
							serverName = secondaryServer;
							port = secondaryPort;
							isPrimaryServer = false;
						}
						else
						{
							serverName = primaryServer;
							port = primaryPort;
							isPrimaryServer = true;
						}
						System.out.println("Now communicating with: " + serverName + ", " + "port " + port + ".\n");
						JokeLogger.getInstance().logMessage("Now communicating with: " + serverName + ", " + "port " + port + ".\n");						
					}
					//no secondary server being used
					else
						System.out.println("No secondary server being used.\n");
				}
				if(!input.toLowerCase().equals("quit") && !input.toLowerCase().equals("s"))
				{
					//processes input as command
					ProcessCommand(serverName,input,port);
				}				
			}
			while (!input.equals("quit"));
			
		//check to determine whether to keep going, will loop forever unless 'quit' given as input.							
		}
		catch (IOException ioe)
		{
			//prints stack trace displaying exception that occured along with line number where error occured, and call stack.
			System.out.println(ioe.getMessage());
			StringWriter sw = new StringWriter();
			ioe.printStackTrace(new PrintWriter(sw));
			JokeLogger.getInstance().logMessage(sw.toString());
			JokeLogger.getInstance().logMessage(ioe.getMessage());
		}		
		
		System.out.println("Now exiting program.");
		JokeLogger.getInstance().logMessage("Now exiting program.");
	}
		//processes given input on specified server
		public static void ProcessCommand(String serverName,String input,int port)
		{
			Socket sock;
			try
			{
			//creates socket on specified port to connect to server.
			sock = new Socket(serverName,port);	
			PrintStream toServer = new PrintStream(sock.getOutputStream());
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//sends input to server
			toServer.println(input);
			//get server response.
			String textFromServer = fromServer.readLine();
			if (textFromServer != null)
			{
				System.out.println(textFromServer);
				JokeLogger.getInstance().logMessage("Response from server");
				JokeLogger.getInstance().logMessage(textFromServer + "\n");
			}
				//close the socket connection
				sock.close();
			}
			catch (Exception ex)
			{
				System.out.println(ex.getMessage());
				StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				JokeLogger.getInstance().logMessage(sw.toString());
				JokeLogger.getInstance().logMessage(ex.getMessage());
			}
			
		}
		
	}
			