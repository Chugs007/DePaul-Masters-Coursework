/*--------------------------------------------------------
1. Omar Chughtai / 1/14/2018:

2. Java Version 1.8.0_161

3. Compilation instructions
javac JokeClient.java

4. Instructions to run program
In separate shell windows run the following:
> java JokeServer
> java JokeClient

All acceptable commands are displayed on the various consoles.

This program can run across machines, so if using client on different machine, must pass in server
ip address as command line argument.

Example:
> java JokeClient                - connects to host at localhost address
> java JokeClient 140.192.1.22   - connect to host at given ip address
> java JokeClient localhost - connects to to host at localhost address
> java JokeClient localhost localhost - connects to host at localhost, and secondaryServer at localhost with the option to switch between the two.
> java JokeClient localhost 140.192.1.22 - connects to host at localhost, and secondaryServer at given ip address for 2nd argument. Can switch between the two.

5.Files needed to run program 
 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java -optional to switch between modes(joke/proverb)

6. Notes:
JokeLog file created in same directory as where class file is located. Can change name if needed.
----------------------------------------------------------*/

import java.io.*;
import java.net.*;
import java.util.UUID;

public class JokeClient
{	
	private static final int ADMIN_PRIMARY_PORT = 4545;
	private static final int ADMIN_SECONDARY_PORT = 4546;
	
	public static void main(String args[])
	{
		String serverName;
		String primaryServer="";
		String secondaryServer="";
		Socket sock = null;
		JokeLogger.getInstance().setFileName("JokeLog1.txt");
		boolean isPrimaryServer = true;
		boolean secondaryServerExists = false;
		int primaryPort = ADMIN_PRIMARY_PORT;
		int secondaryPort = ADMIN_SECONDARY_PORT;
		int port = primaryPort;
		
		System.out.println("Omar Chughtai's Joke Client.");		
		JokeLogger.getInstance().logMessage("Omar Chughtai's Joke Client.");		
		
		//if 2 arguments given, set secondaryServer to 2nd argument and print message for secondary server.	
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
		//default localhost
		else 
			primaryServer = "localhost";

		//set to primary server
		serverName = primaryServer;
		System.out.println("Server one: " + primaryServer + ", Port: " + primaryPort);
		JokeLogger.getInstance().logMessage("Server one: " + primaryServer + ", Port: " + primaryPort);
		if (secondaryServerExists)
		{
			System.out.println("Server two: " + secondaryServer + ", Port: " + secondaryPort + "\n");		
			JokeLogger.getInstance().logMessage("Server two: " + secondaryServer + ", Port: " + secondaryPort + "\n");
		}
			
		//creates UUID to uniquely identify each user client.
		UUID uuid = UUID.randomUUID();
		
		//Creates input using System.in to read from standard input
		//Wraps with InputStreamReader and BufferedReader classes.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try
		{			
			String textFromServer;
			String name;
			boolean firstTime = true;
			String input= "";
			System.out.println("Please enter a user name to connect to server");
			JokeLogger.getInstance().logMessage("Please enter a user name to connect to server");			
			System.out.println();
			System.out.flush();
			//get user input
			name = in.readLine();	
			JokeLogger.getInstance().logMessage(name);			
			//begin do while loop								
			do
			{								
					//Create sockect connection on specified server and port.
					sock = new Socket(serverName,port);
					if (firstTime)
					{
						System.out.println("Connected to server: " + serverName + " Port: " + port + "\n");	
						JokeLogger.getInstance().logMessage("Connected to server: " + serverName + " Port: " + port + "\n");							
						firstTime = false;
					}					
					BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					PrintStream toServer = new PrintStream(sock.getOutputStream());					
					//send uuid and user name to server
					toServer.println(uuid.toString() + " " + name);					
					//get response from server.
					textFromServer = fromServer.readLine();
					if (textFromServer != null)
					{
						System.out.println(textFromServer);
						JokeLogger.getInstance().logMessage("Response from server: " + textFromServer + "\n");
					}
									
				System.out.println("Please hit enter to get next joke/proverb, or 'quit' to exit program.");
				if (args.length == 2)
				{
					System.out.println("Press 'S' to switch to secondary server/back to primary server.\n");
				}
				//get user input
				input = in.readLine();
				//If invalid input detected, loop continously
				while((!input.equals("")) && (!input.toLowerCase().equals("quit")) && (!input.toLowerCase().equals("s")))
				{
					System.out.println("Please hit enter to get next joke/proverb, or 'quit' to exit program.");
					if (args.length == 2)
					{
						System.out.println("Press 'S' to switch to secondary server/back to primary server.\n");
					}
					input = in.readLine();
				}
				//if 's' input, change servers
				if (input.toLowerCase().equals("s"))
				{
					//switch to either primary or secondary server depending on current server.
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
						firstTime = true;
					}
					//no secondary server argument specified, don't switch.
					else
						System.out.println("No secondary server being used.\n");
				}
				
					
				
			}
			//check to determine whether to keep going, will loop forever unless 'quit' given as input.
				while (!input.toLowerCase().equals("quit"));
			System.out.println("Now exiting program");
			JokeLogger.getInstance().logMessage("Now exiting program");
			//close the socket connection
			sock.close();
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
	}
		
		
	}
			
		