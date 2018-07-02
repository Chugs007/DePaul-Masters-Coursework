/*--------------------------------------------------------
1. Omar Chughtai / 1/14/2018:

2. Java Version 1.8.0_161

3. Compilation instructions
javac BlockChain.java

4. Instructions to run program
In separate shell windows run the following(Batch file recommended to make things easier)
> java BlockChain {processId}
processId = integer representing process
need to run three different windows as numberOfProcesses variable is set to 3 currently.
Each processId must be different, and must be in range from 0 to whatever numberOfProcesses is currently.
Last process to run must be process 2 as that triggers the other processes to start the whole blockchain system.


Example:
In three separate windows run
> java BlockChain 0   - starts BlockChain program but waits until process 2 has started to proceed.
> java BlockChain 1   - starts BlockChain program but waits until process 2 has started to proceed.
> java BlockChain 2   - starts BlockChain program and starts the BlockChain system, notifying other processes to begin as well.

5.Files needed to run program 
 a. BlockChain.java
 b. Input text files to read data from
 

6. Notes:
BlockChainLog contains console output from all three processes being run.
File for reading in each process currently hardcoded to one of the BlockInput files(BlockInput + processId) 
Console commands shown after blockchain system runs and creates the blockchain. press enter if not shown.(main method sleeps for 10 seconds to wait for blockchain system to finish.  

----------------------------------------------------------*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.Base64;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;	

import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class BlockChain
{
	//max number of connections in queue allowed.
	private final static int Q_LEN = 6;
	//port bases for the different servers used.
	private static final int publicKeyPortBase = 4710;
	private static final int unverifiedBlockPortBase = 4820;
	private static final int updateBlockChainPortBase	 = 4930;
	//number of Processes that the program requires to run.
	public static final int numberOfProcesses = 3;
	//key used to sign(block id or hash)
	private static PrivateKey privateKey;
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	//boolean variable indicating whether to start system or not, only process 2 will start immediately.
	public static Boolean startSystem = false;
	public static int processId= 0;
	//blocking queue, used to put unverified blocks in.
	public static BlockingQueue<BlockRecord> queue;
	//The current blockchain that each process currently has a copy of.
	public static LinkedList<BlockRecord> Blockchain;		
	//Boolean value indicating whether all public keys have been received and process can continue on.
	public static Boolean publicKeysRead = false;
	
	public BlockChain()
	{
		
	}
	
	public static void main(String[] args)
	{
	
	try
	{
		//HashMap data structure used to create entries for a process("Process + processID") with corresponding public key for that process.
		HashMap<String,PublicKey> processPublicKeyMap = new HashMap<String,PublicKey>();	
		//priority blocking queue initialized with BlockRecordComparator used to prioritize the blocks in the queue.
		queue = new PriorityBlockingQueue<BlockRecord>(10,new BlockRecordComparator());	
		//CountDownLatch used to wait until process reads all public keys(3).
		CountDownLatch cdl = new CountDownLatch(1);
		//initialize blockchain using linkedlist data structure.
		Blockchain = new LinkedList<BlockRecord>();					
		Scanner in = new Scanner(System.in);
								
		if (args.length == 1)
		{
			//Gets the integer value from command argument
			processId = Integer.parseInt(args[0]);		
			System.out.println("PROCESS " + processId + " is now running...");
			//Start all three servers using specified ports and passing in relevant parameters for each.
			PublicKeyServer pks = new PublicKeyServer(publicKeyPortBase + processId, processPublicKeyMap,Q_LEN,cdl);
			new Thread(pks).start();
			UnverifiedBlockServer ubs = new UnverifiedBlockServer(unverifiedBlockPortBase + processId);
			new Thread(ubs).start();
			BlockChainServer bcs = new BlockChainServer(updateBlockChainPortBase + processId);
			new Thread(bcs).start();
		}					
		//program does not allow other number of arguments, must have exactly one otherwise it won't run.
		else
		{
			System.out.println("Invalid number of arguments given, must give 1 integer value indicating process id.");
			return;
		}
		//process 2 can start immediately
		if (processId == 2)
			startSystem = true;
		
		//other processes will keep waiting until signaled to start.
		while (!startSystem)
			Thread.sleep(200);
		startBlockChainSystem(processPublicKeyMap,cdl);	
		//countdownlatch waits here until blockchain of specified size(numberOfRecords) is created.
		//cdl.await();		
		Thread.sleep(10000);
		//Show console commands for user. 
		while (true)
		{			
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("Please select an option from below");
			System.out.println("Enter C to loop through blockchain and tally how many blocks each process has verified.");
			System.out.println("Enter R followed by fileName(space separated) to read records from a file.");
			System.out.println("Enter V to validate blockchain");
			System.out.println("Enter L to see information about each block record");
			String input = in.nextLine();
			System.out.println(input);			
			if (input.toLowerCase().equals("v"))
			{
				verifyBlockChain(processPublicKeyMap);
			}				
			else if (input.toLowerCase().equals("c"))
			{
				creditTally();
			}
			else if (input.toLowerCase().indexOf("r") == 0)
			{
				String fileName = input.substring(2);
				if (fileName.equals(null) || fileName.equals(""))
				{
					System.out.println("Please enter a file name in which you wish to read records from. ex- R filename");
					continue;
				}				
				readFileInput(fileName);				
			}
			else if (input.toLowerCase().equals("l"))
			{
				listRecords();
			}
		}	
		
	}
	catch(Exception ex)
	{
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	}
		
	}

	//Method is responsible for starting the whole BlockChain process.
	public static void startBlockChainSystem(HashMap<String,PublicKey> processPublicKeyMap,CountDownLatch cdl)
	{
		try
		{							
			//create filename string for input file to read from
			String fileName = "BlockInput";
			fileName += processId + ".txt";
			if (processId == 2)
			{
				//create first block, dummy block.
				BlockRecord dummyA = new BlockRecord();
				int blockNumber = 1;		
				String uuid = new String(UUID.randomUUID().toString());
				dummyA.setABlockID(uuid);
				//create xml string of block data and then hash it.
				String blockXMLData  = marshalBlockRecordToXMLString(dummyA);
				//create hash of block xml data.
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(blockXMLData.getBytes());
				byte[] byteHash = md.digest(); 
			
				//create sha256 string by base64 encoding bytes from hash.
				String SHA256String = Base64.getEncoder().encodeToString(byteHash);		
				//set hash and block number value for first block, then add to blockchain.
				dummyA.setASHA256String(SHA256String);
				dummyA.setABlockNum(Integer.toString(blockNumber));	
				//add dummy block to blockchain.
				Blockchain.add(dummyA);
				for (int i=0; i < numberOfProcesses;i++)
				{
					Socket socket = new Socket("localhost",updateBlockChainPortBase + i);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());												
					//create records class and set records to blockChain
					BlockRecords blockRecords = new BlockRecords();
					blockRecords.setBlockRecords(Blockchain);
					//marshall records object to xml and send to each BlockChain Server.
					JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecords.class);
					Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
					StringWriter sw = new StringWriter();						
					jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);						
					jaxbMarshaller.marshal(blockRecords, sw);
					String stringXml = sw.toString();						
					oos.writeObject(stringXml);
					
					oos.flush();
					oos.close();
				}
							
			}
			
			sendPublicKey();
			cdl.await();
			//read input from file with specified fileName.
			readFileInput(fileName);	
			//Create new thread to handle unverified blocks inside queue.
			BlockVerificationWorker bvw = new BlockVerificationWorker(processPublicKeyMap,updateBlockChainPortBase,privateKey);
			bvw.start();					
		}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}		
	}
	
	//Checks to see if BlockChain has a specific block record, using Block Id as identifier.
	public static boolean containsRecord(BlockRecord record)
	{			
		for(BlockRecord br : Blockchain)
		{
			if (br.getABlockID().equals(record.getABlockID()))
				return true;
			
			
		}
		return false;
	}
	
	//Method responsible for verifying the blockchain.
	public static void verifyBlockChain(HashMap<String,PublicKey> processPublicKeyMap)
	{
		try
		{
			int size = Blockchain.size();
			int index = 0;
			BlockRecord record;
			BlockRecord previousRecord = Blockchain.get(index);			
			String previousHash = Blockchain.get(index).getASHA256String();
			index++;	
			
			while (index < size)
			{
				record = Blockchain.get(index);
				System.out.println("Block #" + record.getABlockNum());
				System.out.println("Checking hash value,signed-SHA-256 signature, and signed block id for correct values");
				String blockDataString = marshalBlockRecordToXMLString(record);
				blockDataString = blockDataString.substring(blockDataString.indexOf("<Seed>"));
				String currentHash = record.getASHA256String();
				byte[] currentHashBytes = Base64.getDecoder().decode(currentHash);
				String combinedString = previousHash + blockDataString;
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(combinedString.getBytes());				
				byte[] calculatedHashBytes = md.digest();				
				String calculatedHash =  Base64.getEncoder().encodeToString(calculatedHashBytes);				
				System.out.println("calculated hash(Base64 encoded)= " + calculatedHash);
				System.out.println("current hash(Base64 encoded) = " + currentHash);					
				if (currentHash.equals(calculatedHash))
					System.out.println("Hashes are equal");
				else
					System.out.println("Hashes not equal");
				String stringOut = DatatypeConverter.printHexBinary(calculatedHashBytes); 
				int workNumber = Integer.parseInt(stringOut.substring(0,4),16);
				if (workNumber < 20000)
				{
					System.out.println("Calculated hash solves the puzzle");
				}
				else
				{
					System.out.println("Calculated hash does not solve the puzzle");
				}
				String signedHash = record.getASignedSHA256();
				byte[] signedBytes = Base64.getDecoder().decode(signedHash);
				boolean verifySignature = verifySig(currentHashBytes,processPublicKeyMap.get(record.getAVerificationProcessID()),signedBytes);
				if (verifySignature)
				{
					System.out.println("Signed SHA256 signature is correct, used public key to verify");
				}
				else
				{
					System.out.println("Signed SHA256 signature is not correct.");
					
				}
				
				String blockID = record.getABlockID();
				String signedBlockID = record.getASignedBlockID();				
				byte[] signedBlockIDBytes = Base64.getDecoder().decode(signedBlockID);
				
				boolean verfiySignedBlockID = verifySig(blockID.getBytes(),processPublicKeyMap.get(record.getACreatingProcessID()),signedBlockIDBytes);
				if (verfiySignedBlockID)
				{
					System.out.println("Signed Block-ID signature is correct, used public key to verify");
				}
				else
				{
					System.out.println("Signed Block-ID signature is not correct.");
					
				}
				previousHash = record.getASHA256String();
				index++;					
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	//Method responsible for tallying the number of times a process has verified an unverified block.
	public static void creditTally()
	{
		int processOneCounter = 0;
		int processTwoCounter = 0;
		int processThreeCounter = 0;
			
		for (BlockRecord br : Blockchain)
		{
			if (br.getABlockNum().equals("1"))
				continue;
			String process = br.getAVerificationProcessID();
			if (process.equals("Process 0"))
				processOneCounter++;
			else if (process.equals("Process 1"))
				processTwoCounter++;
			else if (process.equals("Process 2"))
				processThreeCounter++;					
		}
		
		System.out.println("Verification tally for each process");
		System.out.println("Process 0 : " + processOneCounter);
		System.out.println("Process 1 : " + processTwoCounter);
		System.out.println("Process 2 : " + processThreeCounter);				
	}
	
	//List all block records from blockchain, line by line.
	public static void listRecords()
	{
		System.out.println("Now listing each block record line by line");
		for(BlockRecord br: Blockchain)
		{			
			System.out.println("Block # " + br.getABlockNum() + " " + br.getATimeStamp() + " " + br.getIFname() + " " + br.getILname() + " " + br.getRDOB() + " " + br.getISSN() + " " + br.getRDiagnosis() + " " + br.getRTreatment() + " " + br.getRRx());			
		}			
	}
	
	//Sends the public key of current process to each of the processes running, including slef.
	public static void sendPublicKey()
	{
		try
		{
			for (int i=0; i < numberOfProcesses; i++)
			{
				//create public key record structure.
				PublicKeyRecord record = new PublicKeyRecord();
				//Create socket connection for specified process 
				Socket s = new Socket("localhost",publicKeyPortBase + i);
				//Generate public and private keys.
				KeyPair keyPair = generateKeyPair(999);
				PublicKey publicKey = keyPair.getPublic();
				privateKey = keyPair.getPrivate();
				//base64 encode the public key and set to publickey field of record.
				String encodedKey =  Base64.getEncoder().encodeToString(publicKey.getEncoded());
				record.setProcessID(processId);
				record.setPublicKey(encodedKey);
				//marshal public record into string xml and send to each public key server running.
				JAXBContext jc = JAXBContext.newInstance(PublicKeyRecord.class);
				Marshaller marshaller = jc.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				StringWriter sw = new StringWriter();
				marshaller.marshal(record,sw);
				String stringXml = sw.toString();	
				//Create object output stream using socket's output stream.
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(stringXml);
				oos.flush();
				oos.close();
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();		
		}	
	}
	
	//Reads record entries from the specified file.
	public static void readFileInput(String fileName)
	{
		try
		{
			int itemsProcessed = 0;
			//create reader object to read file.
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String[] tokens = new String[10];		
			String uuid;				
			String input = reader.readLine();
			//Keep looping while there is a line of input available.
			while (input != null)
			{			
				//create timestamp using current date.
				Date date = new Date();
				String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
				String TimeStampString = T1 + "." + processId + "\n"; 
				
				//create block record 
				BlockRecord record = new BlockRecord();
				//create UUID to be used as block id.
				uuid = new String(UUID.randomUUID().toString());
				record.setABlockID(uuid);
				String processIdString = "Process " + processId;
				record.setACreatingProcessID(processIdString);
				//sign the block id using the private key.
				byte[] signedData = signData(uuid.getBytes(),privateKey);				
				String signedBlockID = Base64.getEncoder().encodeToString(signedData);
				record.setASignedBlockID(signedBlockID);
				//Split string input by given delimiter
				tokens = input.split(" +");
				//read from tokens array, and put into BlockRecord object.
				record.setATimeStamp(TimeStampString);
				record.setIFname(tokens[0]);
				record.setILname(tokens[1]);
				record.setRDOB(tokens[2]);
				record.setISSN(tokens[3]);
				record.setRDiagnosis(tokens[4]);
				record.setRTreatment(tokens[5]);
				record.setRRx(tokens[6]);
				record.setBSeed("");
				//create xml string of block data.
				String stringXML = marshalBlockRecordToXMLString(record);
				String stringXMLBlockData = stringXML.substring(stringXML.indexOf("<Seed>")); 
				//create hash of block data, base64 encode and set to record header.
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update (stringXMLBlockData.getBytes());
				byte[] byteData = md.digest();
				String SHA256String = Base64.getEncoder().encodeToString(byteData);
				record.setASHA256String(SHA256String);
				//sign hash using private key and set to header as well.
				byte[] signedHash = signData(byteData,privateKey);
				String SignedSHA256String = Base64.getEncoder().encodeToString(signedHash);
				record.setASignedSHA256(SignedSHA256String);
				//creat xml string again, this time with header information.
				stringXML = marshalBlockRecordToXMLString(record);				
				//base 64 encode the xml string, and send to each unverified block server for each process running.
				String base64EncodeXml = Base64.getEncoder().encodeToString(stringXML.getBytes());
				for (int i=0; i < numberOfProcesses; i++)
				{
					Socket socket = new Socket("localhost",unverifiedBlockPortBase + i);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					System.out.println("Sending block record as xml string to all processes to verify");
					oos.writeObject(base64EncodeXml);
					oos.flush();
					oos.close();				
				}							
				itemsProcessed++;
				//read next line to see if there is any more records to process.
				input = reader.readLine();			
			}		
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();		
		}
	}
	
	//Marshals the block record into xml string form, used to send over to all processes including self.
	public static String marshalBlockRecordToXMLString(BlockRecord record)
	{		
		String stringXml = "";
		try
		{		
			//create necessary objects to marshal data.  
			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			StringWriter sw = new StringWriter();		
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);		
			//marshal specified record into string form.
			jaxbMarshaller.marshal(record, sw);
			stringXml = sw.toString();			
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		return stringXml;		
	}
	
	//Method responsible for creating a random string used in the work algorithm.
	public static String randomAlphaNumeric(int count) 
	{
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
		int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
		builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	

   //Method used to sign data(hash or whatever(block id))
   public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initSign(key);
    signer.update(data);
    return (signer.sign());
  }
  
   //Method used to verify a signature(signed data) given a public key and original data(before being signed)
   public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initVerify(key);
    signer.update(data);

    return (signer.verify(sig));
  }
    
	//Method used to generate a public and private key using a seed value.
  	public static KeyPair generateKeyPair(long seed) throws Exception {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
    rng.setSeed(seed);
    keyGenerator.initialize(1024, rng);
    return (keyGenerator.generateKeyPair());
  }
}

//Class responsible for verifying unverified blocks from queue, performing work on them, and adding them to the blockchain once work is completed.
class BlockVerificationWorker extends Thread
	{
		private HashMap<String,PublicKey> processPublicKeyMap;
		private PrivateKey privateKey;
		private int updateBlockChainPortBase;
		
		public BlockVerificationWorker(HashMap<String,PublicKey> processPublicKeyMap,int updateBlockChainPortBase,PrivateKey privateKey)
		{
			this.processPublicKeyMap = processPublicKeyMap;
			this.privateKey = privateKey;
			this.updateBlockChainPortBase = updateBlockChainPortBase;
		}
		
		
		public void run()
		{
			
			try
			{
				//loop continously
				while (true)
				{					
				//blocks until item is available. 200 ms timeout, returns null if nothing in queue
				BlockRecord record =  BlockChain.queue.poll(200,TimeUnit.MILLISECONDS);
				
				//return back to top of while loop if record is null
				if (record == null)
				{								
					continue;						
				}
				
				//create copy of current blockchain, used to check for modification.
				LinkedList<BlockRecord> localChain = new LinkedList<BlockRecord>();
				for(BlockRecord br : BlockChain.Blockchain)
				{
					localChain.add(br);
					
				}									
				
				//if record is already inside blockchain, ignore record and get next one.
				if(BlockChain.containsRecord(record))
				{
					System.out.println("Precheck: block with id : " + record.getABlockID() +  "has already been verified.");	
					continue;					
				}
				
				System.out.println("Retreived record for queue");			
							
				//Get block id and signed block id.
				String uuid = record.getABlockID();		
				String signedBlockId = record.getASignedBlockID();
				//base64 decode signed block id.
				byte[] signedUuidBytes = Base64.getDecoder().decode(signedBlockId);
				String creatingProcessID = record.getACreatingProcessID();
				//Get creating process's public key and use it to verify the signed block id.
				PublicKey publicKey = processPublicKeyMap.get(creatingProcessID);
				boolean verified = BlockChain.verifySig(uuid.getBytes(),publicKey,signedUuidBytes);
				if (!verified)				
					throw new Exception("Unverified block has corrupt data, signed uuid does not match uuid.");			
				//Gets the sha256 hash and signedsha256 hash.  get base64 decoded bytes and validate signed hash using public key again.
				String sha256String = record.getASHA256String();		
				byte[] sha256Bytes = Base64.getDecoder().decode(sha256String);
				String signedSha256String = record.getASignedSHA256();
				byte[] signedSha256Bytes = Base64.getDecoder().decode(signedSha256String);
				verified = BlockChain.verifySig(sha256Bytes,publicKey, signedSha256Bytes);
				if (!verified)
					throw new Exception("Unverified block has corrupt data, signed SHA256 does not match SHA256 string for block.");								
				//set blocknumber to next number in chain.
				int blockNumber = localChain.size() + 1;
				boolean workDone = false;
				//loop continously while work is not done.
				while (!workDone)
				{
					//if blockchain contains record, stop doing work and break out of loop.					
					if(BlockChain.containsRecord(record))
					{
						System.out.println("block with id : " + uuid +  "has already been verified.");
						workDone = true;						
						break;
					}
									
					Thread.sleep(100);
					//get a random string to try.
					String randomString = BlockChain.randomAlphaNumeric(8);
					//set seed value with this random string.
					record.setBSeed(randomString);			
					//Get the previousHash and string xml data of the block(only the block data part not header)
					String previousHash = localChain.getLast().getASHA256String();
					String stringXML = BlockChain.marshalBlockRecordToXMLString(record);	
					stringXML = stringXML.substring(stringXML.indexOf("<Seed>"));			
					//combine previousHash and block data string together and create a hash.
					String combinedString = previousHash + stringXML;
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					md.update(combinedString.getBytes());
					byte[] byteHash = md.digest();
					String stringOut = DatatypeConverter.printHexBinary(byteHash); 
					//Get leftmost 16 bits of hash to create a 16 bit number.
					int workNumber = Integer.parseInt(stringOut.substring(0,4),16);
					//see if number is in range.
					if (workNumber < 20000)
					{
						//solution found.
						workDone = true;		
						//set new hash value, base64 encode.
						String newSHA256String = Base64.getEncoder().encodeToString(byteHash);
						//sign has with private key of verifying process.
						byte[] signedData = BlockChain.signData(byteHash,privateKey);
						String newSignedString = Base64.getEncoder().encodeToString(signedData);			
						//set header data for record.									
						record.setASHA256String(newSHA256String);
						record.setASignedSHA256(newSignedString);								
						System.out.println("Solution found!");					
						//check if blockchain has changed since beginning, if so have to start over again.
						boolean modified = false;
						//check for modification by examining size of each blockchain(local and global).
						if(localChain.size() != BlockChain.Blockchain.size())
						{
							modified = true;
						}
						else
						{
							//check again for modification, this time ensuring that records for each blockchain are the same.
							for(int i=0; i < localChain.size();i++)
							{
								String s1 = localChain.get(i).getABlockID();
								String s2 = BlockChain.Blockchain.get(i).getABlockID();
								if (!s1.equals(s2))
								{
									modified = true;
									break;
								}
							}						
						}
						//if modified, need to check whether block record is already inside updated blockchain(global).
						if (modified == true)
						{						
							System.out.println("Blockchain has been modified.");
							if (BlockChain.containsRecord(record) == false)
							{
								System.out.println("Block not in modified blockchain, starting over again");
								workDone = false;						
								//create a clone of current blockchain and get a new blockNumber 
								localChain = new LinkedList<BlockRecord>();	
								for(BlockRecord br : BlockChain.Blockchain)
								{
									localChain.add(br);
									
								}
								blockNumber = localChain.size() + 1;													
							}					
						}
						else
						{
							//verified block can now be added.
							record.setAVerificationProcessID( "Process " + Integer.toString(BlockChain.processId));	
							System.out.println("Adding new block " + record.getABlockID() + " to blockchain");							
							//set the block number for this block.
							record.setABlockNum(Integer.toString(blockNumber));								;
							BlockChain.Blockchain.add(record);					
							//broadcast new blockchain to all processes.
							System.out.println("Sent update blockchain to all processes.");
							for (int i=0; i < BlockChain.numberOfProcesses;i++)
							{
								Socket socket = new Socket("localhost",updateBlockChainPortBase + i);
								ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());												
								//create records class and set records to blockChain
								BlockRecords blockRecords = new BlockRecords();
								blockRecords.setBlockRecords(BlockChain.Blockchain);
								//marshall records object to xml and send to each BlockChain Server.
								JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecords.class);
								Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
								StringWriter sw = new StringWriter();						
								jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);						
								jaxbMarshaller.marshal(blockRecords, sw);
								String stringXml = sw.toString();						
								oos.writeObject(stringXml);
								
								oos.flush();
								oos.close();
								}
							}					
						}											
					}		
				}
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
	}


//Class represents a group of block records. Annotated with JAXB annotation so that it can be marshalled to xml and unmarshalled back
//to a object.
@XmlRootElement
class BlockRecords
{	
	List<BlockRecord> Records;
	
	@XmlElement(name="BlockRecord")
	public void setBlockRecords(List<BlockRecord> records)
	{
		Records = records;
	}
	
	public List<BlockRecord> getBlockRecords()
	{
		return Records;	
	}
}

//Class represents a block record. Header part delineated from block data by "A" prefix used in getter and setter methods.
//Annotated with JAXB annotation so that it can be marshalled to xml and unmarshalled back
//to a object.
@XmlRootElement
class BlockRecord
{
	String SHA256String; 
	String SignedSHA256;	 
	String CreatingProcessID;
	String VerificationProcessID;		
	String BlockID;
	String SignedBlockID;	
	String BlockNum;	
	String TimeStamp;
	String Seed;
	String Fname;
	String Lname;
	String SSNum;
	String DOB;
	String Diagnosis;
	String Treatment;
	String Rx;
  
  public String getASHA256String()
  {
	  return SHA256String;
  }
  
  @XmlElement(name = "SHA256")
  public void setASHA256String(String sha256String)
  {
	  SHA256String = sha256String;
  }
  
  public String getASignedSHA256()
  {
	  return SignedSHA256;
  }
  
  @XmlElement( name = "SignedSHA256" ) 
  public void setASignedSHA256(String signedSHA256)
  {
	  SignedSHA256 = signedSHA256;
  }
  
  public String getBSeed()
  {
	  return Seed;
	  
  }
  
   @XmlElement( name = "Seed" ) 
  public void setBSeed(String seed)
  {
	  Seed = seed;
  }
   
  public String getAVerificationProcessID()
  {
	  return VerificationProcessID;
  }
  
   @XmlElement( name = "VerificationProcessID" ) 
  public void setAVerificationProcessID(String processID)
  {
	  VerificationProcessID = processID;
  }
  
  public String getABlockID()
  {
	  return BlockID;
  }
  
   @XmlElement( name = "BlockID" )  
  public void setABlockID(String blockID)
  {
	  BlockID = blockID;
  }
  
  public String getASignedBlockID()
  {
	  return SignedBlockID;
  }
  
   @XmlElement( name = "SignedBlockID" ) 
  public void setASignedBlockID(String signedBlockID)
  {
	  SignedBlockID = signedBlockID;
  }
  
  public String getACreatingProcessID()
  {
	  return CreatingProcessID;
  }
  
   @XmlElement( name = "CreatingProcessID" ) 
  public void setACreatingProcessID(String id)
  {
	  CreatingProcessID = id;
	  
  }
  
  public String getATimeStamp()
  {
	  return TimeStamp;
  }
  
   @XmlElement( name = "TimeStamp" ) 
  public void setATimeStamp(String time)
  {
	  TimeStamp = time;
  }
  
  public String getABlockNum()
  {
		return BlockNum;
  }
  
   @XmlElement( name = "BlockNum" )  
  public void setABlockNum(String blocknumber)
  {
	  BlockNum = blocknumber;
  }  
  
  public String getIFname()
  {
	  return Fname;
  }
  
  @XmlElement( name = "Fname" ) 
  public void setIFname(String name)
  {
	  Fname = name;
  }
  
  public String getILname()
  {
	  return Lname;
  }
  
  @XmlElement( name = "Lname" )  
  public void setILname(String lName)
  {
	  Lname = lName;
  }
  
  public String getISSN()
  {
	  return SSNum;
  }
  
   @XmlElement( name = "SSNum" ) 
  public void setISSN(String SSN)
  {
	  SSNum = SSN;
  }
  
  public String getRDOB()
  {
	  return DOB;
  }
  
   @XmlElement( name = "DOB" ) 
  public void setRDOB(String dob)
  {
	  DOB = dob;
  }
  
  public String getRDiagnosis()
  {
	return Diagnosis;
  }  
  
   @XmlElement( name = "Diagnosis" ) 
  public void setRDiagnosis(String diagnosis)
  {
	  Diagnosis = diagnosis;
  }
  
  public String getRTreatment()
  {
	  return Treatment;
  }
  
   @XmlElement( name = "Treatment" ) 
  public void setRTreatment(String treatment)
  {
	  Treatment = treatment;
  }
  
  public String getRRx()
  {
	  return Rx;
  }
  
   @XmlElement( name = "Rx" )  
  public void setRRx(String rx)
  {
	  Rx = rx;
  }
  
}

//Class represents a public key record used to keep a public key value with corresponding process.
//Annotated with JAXB annotation so that it can be marshalled to xml and unmarshalled back
//to a object.
@XmlRootElement
class PublicKeyRecord
{
	int ProcessID;
	String PublicKey;
	 
	public int getProcessID()
	{
		return ProcessID;
	}
	
	@XmlElement 
	public void setProcessID(int value)
	{
			ProcessID = value;
	}	

	public String getPublicKey() 
	{
		return PublicKey;
	}
	
    @XmlElement
    public void setPublicKey(String value)
	{
		this.PublicKey = value;
	}

}

//Comparator object used to sort BlockRecords by timestamp. Prioritized so that earlier timestamp values, get retreived first from queue.
class BlockRecordComparator implements Comparator<BlockRecord>
{
	public int compare(BlockRecord a, BlockRecord b)
	{
		//null entries go after non null entries. should never happen since record should not be null.
		if (a== null)
		{	
			if (b == null)
				return 0;
			else
				return 1;
		}
		else if (b == null)
			return -1;
		else
			return a.getATimeStamp().compareTo(b.getATimeStamp());		
	}
	
}


//Class responsible for serving as a server to accept connections from incoming clients, which are sending public key records.
class PublicKeyServer implements Runnable
{
	//fields used to set up server.
	private int port;
	private int q_len;
	private HashMap<String,PublicKey> processPublicKeyMap;	
	private static ServerSocket socket;
	private CountDownLatch cdl;
	
	public PublicKeyServer(int port,HashMap<String,PublicKey> processPublicKeyMap,int q_len,CountDownLatch cdl)
	{
		this.port = port;
		this.processPublicKeyMap = processPublicKeyMap;
		this.q_len = q_len;		
		this.cdl = cdl;
	}
		
	public void run()
	{
		try
		{
			//create server socket to listen on for incoming connections.
			socket = new ServerSocket(port,q_len);
			System.out.println("Process " + BlockChain.processId + " public key server running on port " + port);
			//loop continously to listen for connections.
			while (true)
			{
				//blocks until client connection made.
				Socket s = socket.accept();				
				//start worker thread to handle request.
				new PublicServerWorker(s,processPublicKeyMap,cdl).start();				
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	
	
	//Worker class responsible for processing request made from client, which sends a publickey record containing the public key of a 
	//specified process.
	class PublicServerWorker extends Thread
	{
		
		private Socket socket;
		private HashMap<String,PublicKey> processPublicKeyMap;
		private CountDownLatch cdl;
		
		public PublicServerWorker(Socket socket, HashMap<String,PublicKey> processPublicKeyMap, CountDownLatch cdl)
		{
			this.socket = socket;
			this.processPublicKeyMap = processPublicKeyMap;				
			this.cdl = cdl;
		}
		
		public void run()
		{
			try
			{				
				//Read from ObjectInputStream
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				//get xml string version of public key record object.
				String xmlPublicKey = (String) ois.readObject();						
				JAXBContext jaxbContext = JAXBContext.newInstance(PublicKeyRecord.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(xmlPublicKey);
				//unmarshal string to public key reocrd.
				PublicKeyRecord br = (PublicKeyRecord)unmarshaller.unmarshal(reader);
				String key = br.getPublicKey();			
				int pID = br.getProcessID();
				String processId =   "Process " + Integer.toString(pID);
				//base64 decode key(which is base64 encoded)
				byte[] decodedKey = Base64.getDecoder().decode(key);
				//Generate the public key using the decodedKey bytes.
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(decodedKey));			
				System.out.println("Retreived public key for Process " + processId + " : " + publicKey.getEncoded());
				//put public key value created into hashmap with corresponding processId.
				processPublicKeyMap.put(processId,publicKey);
				//if received all three public keys, countdown now and can proceed on.
				if (processPublicKeyMap.size() == BlockChain.numberOfProcesses)
				{
					cdl.countDown();
				}
				
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
		
	}
	
}
	
	
	//Class represents a server responsible for handing incoming connections containing unverified blocks.
	class UnverifiedBlockServer implements Runnable
	{
		//fields used to set up server.
		private HashMap<String,PublicKey> processPublicKeyMap;	
		private int port;		
		private ServerSocket servSock;		
	
		
		public UnverifiedBlockServer(int port)
		{
			this.port = port;						
		}
		
		public void run()
		{
			try
			{
			while (true)
			{
				//create server socket listen on.
				servSock = new ServerSocket(port,6);
				System.out.println("Unverified Block Server up and running on port " + port );
				//loop continously to listen for incomingn connections
				while(true)
				{
					//blocks until client connection is made.
					Socket socket = servSock.accept();
					//handle each client connection in separate thread using UnverifiedBlockWorker class.
					new UnverifiedBlockWorker(socket).start();
				}						
			}
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}			
			
		}
		
		//Class responsible for processing unverified blocks, and putting them inside the priority blocking queue.
		class UnverifiedBlockWorker extends Thread
		{
			private Socket socket;		
			
			public UnverifiedBlockWorker(Socket socket)
			{
				this.socket = socket;				
			}
			
			public void run()
			{
				try
				{
					//Creates ObjectInputStream object using socket's input stream to get input.
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					String input = (String) ois.readObject();					
					//base 64 decode the string put.
					byte[] decoded = Base64.getDecoder().decode(input.getBytes());
					//Create String represented decoded bytes.
					String inputXml = new String(decoded);
					//Unmarshall the xml string into the BlockRecord object.
					JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();			
					StringReader reader = new StringReader(inputXml);
					BlockRecord record = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);					
					System.out.println("Retreived block record for unverified block created by " + record.getACreatingProcessID());
					//put the unverified block record into the priority blocking queue.
					BlockChain.queue.put(record);	
					System.out.println("unverified block record with id: " + record.getABlockID() + "has been put inside the queue");
					ois.close();				
			}
			catch(Exception ex)
				{
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			
			}	
		
		}
	}
	
	//Class represents a server responsible for handing incoming connections containing a updated BlockChain.
	class BlockChainServer implements Runnable
	{			
			private int port;			
			private ServerSocket servSock;
					
			
			public BlockChainServer(int port)
			{
				this.port = port;				
			}
			
			public void run()
			{
				try
				{
				//create ServerSocket object to listen on.
				servSock = new ServerSocket(port,6);
				System.out.println("Block Chain Server up and running on port " + port );
				//loop continously to listen for incoming connections.
				while (true)
				{
					//Blocks until client connection is made.
					Socket socket = servSock.accept();	
					//set boolean field to true so that process 0 and process 1 can start.
					if (BlockChain.startSystem == false)
						BlockChain.startSystem = true;
					//Processes client request in a new thread using the BlockChainWorker class.
					new BlockChainWorker(socket).start();
				}
				}
				catch(Exception ex)
				{
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
			
			
		//Class responsible for handling incoming client connection with updated BlockChain.
		class BlockChainWorker extends Thread
		{
			private Socket socket;				
			
			public BlockChainWorker(Socket socket)
			{
				this.socket = socket;				
			}
			
			public void run()
			{
				try
				{		
				//Create ObjectInputStream object using socket's input stream.
				ObjectInputStream oos = new ObjectInputStream(socket.getInputStream());
				//read from input stream and get string object.
				String input = (String) oos.readObject();		
				//Unmarsal the string input from xml to BlockRecords class 
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecords.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();			
				StringReader reader = new StringReader(input);
				BlockRecords records = (BlockRecords) jaxbUnmarshaller.unmarshal(reader);
				//Gets the blockChain list and set to newBlockChain;
				LinkedList<BlockRecord> newBlockChain = new LinkedList<BlockRecord>(records.getBlockRecords());				
				System.out.println("New block chain received, now updating local blockchain with new one.");
				//update current blockchain with new blockchain.
				BlockChain.Blockchain = newBlockChain;				
				
				
				//if process 0, then write new blockchain to xml file.
				if (BlockChain.processId == 0)
				{
					//Create objects to marshal BlockRecords class to xml string.
					JAXBContext jc = JAXBContext.newInstance(BlockRecords.class);
					Marshaller jaxbMarshaller = jc.createMarshaller();		
					jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);					
					//created bufferedwriter object using xml file to write to.
					BufferedWriter writer = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
					jaxbMarshaller.marshal(records,writer);
					writer.close();
					System.out.println("Successfully wrote new blockchain to BlockChainLedger xml file.");					
				}
						
					oos.close();				
				}
				catch(Exception ex)
				{
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}
		