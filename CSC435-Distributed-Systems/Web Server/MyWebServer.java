/*--------------------------------------------------------
1. Omar Chughtai / 2/1/2018:

2. Java Version 1.8.0_161

3. Compilation instructions
javac MyWebServer.java

4. Instructions to run program
In separate shell windows run the following:
> java MyWebServer
This will run server on port 2540, where it will listen for incoming connections.


5.Files needed to run program 
 a. MyWebServer.java
 b.checklist-mywebserver.html

6. Notes:
servlog.txt generated if not already existing when running MyWebServer.  
Text created in main method of MyWebServer class, can change filename if you want to, will 
create text file in directory of where MyWebServer.java file is run from.
----------------------------------------------------------*/

import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

//Class acts as a web server running on port 2540, and handles client request by retrieving relevant file, directory, or response(to //process form), and sends back response to client.  Handles HTTP requests sent from client, and response includes HTTP header information
//along with response data.
public class MyWebServer
{
	public static void main(String args[])  throws IOException
	{
		int q_len = 6;
		//use port 2540 to listen for incoming client connections.
		int port = 2540;
		Socket sock;
		//create instance of ServerLoggere to log any relevant messages to text file.
		ServerLogger logger = new ServerLogger("serverlog.txt");
		ServerSocket servsock = new ServerSocket(port,q_len);
		
		System.out.println("Omar Chughtai's Web Server running at port " +  port + ".");
		logger.logMessage("Omar Chughtai's Web Server running at port " +  port + ".");
		while (true)
		{
			//blocks until client connects, assigns sock object to handle specific client request.
			sock = servsock.accept();
			//creates new thread to work on request.
			new ServerWorker(sock,logger).start();
		}		 	
	}
}

//Class represents worker thread responsible for handling client request.
class ServerWorker extends Thread
{
	Socket sock;
	ServerLogger logger;
	
	ServerWorker (Socket s,ServerLogger logger)	
	{
		sock = s;
		this.logger = logger;
	}
	
	public void run()
	{
		PrintStream out = null;
		BufferedReader in = null;
		String fileType = "";
		try
		{
			//create output and input streams used to read and write data from socket connection
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));		
			//get client request.
			String clientInput = in.readLine();
			System.out.println("Client request received: " + clientInput);
			logger.logMessage("Client request received: " + clientInput);
			//get index of 'GET' string in request.
			int getIndex = clientInput.indexOf("GET");
			//check if valid get request made, no 'GET' means invalid request made.
			if (getIndex == 0)
			{			
				//Split request into string array by white space character
				String[] splitRequest = clientInput.split(" ");				
				//Second item in array should be requested material, after 'GET'
				String request = splitRequest[1];	
				//Handle text file request.
				//Check for attempt to get file from directory above current directory which is not good.				
				if (request.contains("../"))
				{													
					String message = "Request denied, must make a valid request inside of directory.";
					errorMessage(message,"403 Forbidden", fileType,out);					
				}
				else if (request.endsWith(".txt") == true)
				{									
					fileType = "text/plain";
					sendFile(request,out,fileType);
				}
				//Handle html file request.
				else if (request.endsWith(".html") == true)
				{
					fileType = "text/html";
					sendFile(request,out,fileType);
				}
				//Handle directory request.
				else if(request.endsWith("/") == true) 
				{									
					fileType = "text/html";
					sendDirectoryPage(request,out,fileType);					
				}
				//Handle form submission for add numbers.
				else if (request.contains("addnums.fake-cgi"))
				{
					fileType = "text/html";
					addnums(out,request,fileType);
					
				}
				//other requests, example: favicon file
				else 
				{
					fileType = "text/plain";
					sendFile(request,out,fileType);
				}
			}
			//bad request made, send error response 400 Bad Request to client.
			else
			{										
				String message = "Bad request given, must start with 'GET' followed material to request from server"; 
				errorMessage(message,fileType," 400 Bad Request",out);				
			}			
			sock.close();
			in.close();
			out.close();
		}
		//catch any IOExceptions
		catch (IOException ex)
		{						 
			System.out.println(ex.getMessage());
			logger.logMessage(ex.getMessage());	
		}		
	}

	//Method response for getting specified file and sending it back to the client.
	public void sendFile(String fileName, PrintStream out,String fileType)
	{	
		try
		{
		//handle beginning '/' case.
		if (fileName.startsWith("/"))
		{			
			fileName = fileName.substring(1);			
		}
		//Used to retreive file length, if file does not exist throw FileNotFoundException.
		File f = new File(fileName);
		if (!f.exists())
			throw new FileNotFoundException("Specified file " + fileName + " does not exist in directory!");
		//FileInputStream instance created to get contents of file.
		FileInputStream fis = new FileInputStream(fileName);	
		int fileLength = (int)f.length();
		//send http header for response with desired information.
		out.print("HTTP/1.1 200 OK\r\n" +
		"Content-Length: " + fileLength + "\r\n" + 
		"Content-Type: " + fileType + "\r\n\r\n");
		
		//create buffer with length of file length.
		byte[] buffer = new byte[fileLength];
		fis.read(buffer);
		//send buffer data to back to client.
		out.write(buffer,0,fileLength);
		out.flush();
		logger.logMessage("Successfully retreived " + fileName + " and sent back to client.");
		//close all streams, done with work.		
		fis.close();		
		}
		catch(Exception ex)
		{			
			//If FileNotFoundException, then send 404 error response indicating resource does not exist.		
			if (ex instanceof FileNotFoundException)
			{				
				errorMessage(ex.getMessage(),fileType,"404 Not Found", out);			
			}
			//send error response, log and print to console window.
			else
			{
				String message = "Server encountered problem in fulfilling request.";				
				errorMessage(message,fileType,"500 Internal Server Error", out);	
				System.out.println("Error detail: " + ex.getMessage());
				logger.logMessage(ex.getMessage());
			}
		}
	}
	
	//Method responsible for building a html file containing all files and directories in a requested directory.
	public void sendDirectoryPage(String directoryName, PrintStream out, String fileType)
	{
		try
		{					
			//Creates file object for directory.
			File f = new File("./" + directoryName + "/");
			//if given directory does not exist throw error.
			if (!f.exists())
				throw new FileNotFoundException("Directory " + directoryName + " does not exist!");
			//List all files and directories in given directory
			File[] files = f.listFiles();
			//Creates bufferedwriter instance to create and write to Index.html file.
			BufferedWriter bw = new BufferedWriter(new FileWriter("Index.html"));
			//add beginning html structure to file.
			bw.write("<html>");
			bw.write("<br><br>");
			bw.write("<body>");		
			bw.write("<h1>Index of " + directoryName + "</h1>");
			bw.write("<a href=\"" + directoryName + "\">Parent Directory</a> <br>");
			
			//loop through all files and directories in given directory, and add each to the index.html
			//page as links that can be clicked on.
			for (File file : files)
			{
				String fileName = file.getName();	
				if (file.isDirectory())
					bw.write("<a href=\"" + fileName + "/\">" + fileName + "/</a> <br>");
				else
					bw.write("<a href=\"" + fileName + "\">" + fileName + "</a> <br>");
			}
			
			bw.flush();
			//Finish rest of html file by writing ending html structure.
			bw.write("</body></html>");
			FileInputStream fis = new FileInputStream("Index.html");
			File file = new File("Index.html");
			int fileLength = (int)file.length();
			//Send http header in response with given information.
			out.print("HTTP/1.1 200 OK\r\n" +
				"Content-Length: " + fileLength + "\r\n" + 
				"Content-Type: " + fileType + "\r\n\r\n");
			
			//create buffer with length of file length.
			byte[] buffer = new byte[fileLength];
			fis.read(buffer);
			//send buffer data to back to client.
			out.write(buffer,0,fileLength);			
			out.flush();
			logger.logMessage("Successfully created directory page and send it back to the client.");
			//close all streams, done with work.
			bw.close();
			fis.close();			
		}
		catch(Exception ex)
		{						
			//If FileNotFoundException, then send 404 error response indicating resource does not exist.
			if (ex instanceof FileNotFoundException)
			{				
				errorMessage(ex.getMessage(),fileType,"404 Not Found", out);		
			}
			//send error response, log and print to console window.
			else
			{
				String message = "Server encountered problem in fulfilling request.";
				errorMessage(message,fileType,"500 Internal Server Error", out);
				System.out.println("Error detail: " + ex.getMessage());
				logger.logMessage(ex.getMessage());
			}
		}		
	}
	
	//Processes form submitted by client, adds the numbers together, and 
	//returns result to client.
	private void addnums(PrintStream out,String request, String fileType)
	{
		try
		{
			//Parse input of client submission and retreive username, num1, and num2.
			String userQuery = "?person=";
			String num1Query = "&num1=";
			String num2Query = "&num2=";
			//Find indexes for relevant parts, user, number 1, and number 2. 
			int indexQ = request.indexOf(userQuery);	
			int indexU1 = request.indexOf(num1Query);
			int indexU2 = request.indexOf(num2Query);
			String user = request.substring(indexQ + userQuery.length(),indexU1);
			String num1 = request.substring(indexU1 + num1Query.length(),indexU2);
			String num2 = request.substring(indexU2 + num2Query.length());
			//parse the strings into integers, and perform addition.
			int numberOne = Integer.parseInt(num1);
			int numberTwo = Integer.parseInt(num2);
			int sum = numberOne + numberTwo;
			//Create BufferedWriter instance to write result to html file to send back to client.
			BufferedWriter bw = new BufferedWriter(new FileWriter("FormResult.html"));
			bw.write("<html>");
			bw.write("<br><br>");
			bw.write("<body>");
			bw.write("Dear " + user + ", the sum of " + num1 + " and " + num2 + " is " + sum );
			bw.write("</body></html>");
			bw.flush();
			FileInputStream fis = new FileInputStream("FormResult.html");
			File file = new File("FormResult.html");
			int fileLength = (int)file.length();
			//Create HTTP response header with 200 OK response
			out.print("HTTP/1.1 200 OK\r\n" +
				"Content-Length: " + fileLength + "\r\n" + 
				"Content-Type: " + fileType + "\r\n\r\n");
			
			//create buffer with length of file length.
			byte[] buffer = new byte[fileLength];
			//read into buffer
			fis.read(buffer);
			//send buffer data to back to client.
			out.write(buffer,0,fileLength);
			out.flush();
			logger.logMessage("Successfully processed form data and returned back to client.");						
			//close all streams, done with work.
			bw.close();
			fis.close();			
			
		}
		catch(Exception ex)
		{							
			//if error occured trying to parse integer/s, return 400 bad request error indicating invalid input entered from client.
			if (ex instanceof NumberFormatException)
			{
				String message = "Input entered for numbers(num1 and num2) are invalid, please enter a valid integer for each field.";
				errorMessage(message,fileType,"400 Bad Request", out);				
			}
			//send error response, log and print to console window.
			else
			{
				String message = "Server encountered problem in fulfilling request.";
				errorMessage(message,fileType,"500 Internal Server Error", out);	
				System.out.println("Error detail: " + ex.getMessage());
				logger.logMessage(ex.getMessage());
			}
		}
	}
	
	//Method prints error to console window, logs error to log file specified, and sends error response back to client.
	private void errorMessage(String message,String fileType, String statusCode,PrintStream out)
	{
		//Print to console output, and log message to log file
		System.out.println(message);	
		logger.logMessage(message);
		//Create error response to send back to client.
		int length = message.length();		
		out.print("HTTP/1.1 " + statusCode + " \r\n" +
		"Content-Length: " + length + "\r\n" + 
		"Content-Type: " + fileType + "\r\n\r\n");
		if (out != null)
			out.println(message);		
	}
}


//Class responsible for logging information relevant to MyWebServer such as errors, client requests, response, etc.
class ServerLogger
{
	private static PrintWriter writer;
	private final String fileName;
	
	//Create ServerLogger instance with specified fileName to be used to log messages to.
	public ServerLogger(String fileName)
	{
		this.fileName = fileName;
		
	}
	
	//Method for logging message to file, synchronized so that multiple threads can write to same file at once safely.
	public synchronized void logMessage(String message)
	{			
		try
		{
			//create instance of printwriter on specified fileName, and write message to the file.
			writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true))); 
			writer.println(message);
			writer.flush();
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
		}						
		finally
		{
			writer.close();
		}			
	}	
}





