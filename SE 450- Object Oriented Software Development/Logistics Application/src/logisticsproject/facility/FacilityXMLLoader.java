package logisticsproject.facility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.*;

import logisticsproject.exceptions.FacilityInvalidDataException;

public class FacilityXMLLoader {

	private static final String FACILITYXMLFILEPATH = "/src/logisticsproject/facility/FacilityInventory.xml";
	//Parses xml file containing information for each facility.
	//Need to find a way to open file without full path.
	//custom exceptions or existing?
	public static ArrayList<Facility> LoadXMLFile()
	{
		ArrayList<Facility> facilities = new ArrayList<Facility>();
		try
		{
			String currentdir = System.getProperty("user.dir");
			String fileName = currentdir + FACILITYXMLFILEPATH;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();      
            File xml = new File(fileName);
            if (!xml.exists()) {
                System.err.println("**** XML File '" + fileName + "' cannot be found");
                System.exit(-1);
            }

            Document doc = db.parse(xml);
            doc.getDocumentElement().normalize();

            NodeList facilityEntries = doc.getDocumentElement().getChildNodes();
            for(int i=0; i < facilityEntries.getLength(); i++)  //create a facility
            {
            	 if (facilityEntries.item(i).getNodeType() == Node.TEXT_NODE) {
                     continue;
                 }
                 
                 String entryName = facilityEntries.item(i).getNodeName();
                 if (!entryName.equals("Facility")) {
                     System.err.println("Unexpected node found: " + entryName);
                     break;
                 }
                 
                 Element elem = (Element) facilityEntries.item(i);
                 String facilityName = elem.getElementsByTagName("Name").item(0).getTextContent();
                 String facilityRate = elem.getElementsByTagName("Rate").item(0).getTextContent();
                 String facilityCost = elem.getElementsByTagName("Cost").item(0).getTextContent();
                 HashMap<String, Integer> inventoryItems = new HashMap<String,Integer>();
                 Element inventoryElement = (Element)elem.getElementsByTagName("Inventory").item(0);
                 NodeList inventoryList = inventoryElement.getElementsByTagName("Item");
                 for (int j =0; j < inventoryList.getLength(); j++)
                 {
                	 if (inventoryList.item(j).getNodeType() == Node.TEXT_NODE) {
                         continue;
                     }
                     entryName = inventoryList.item(j).getNodeName();
                     if (!entryName.equals("Item")) {
                         System.err.println("Unexpected node found: " + entryName);
                         break;
                     }
                     
                     Element invElement = (Element) inventoryList.item(j);
                     String itemName = invElement.getElementsByTagName("Name").item(0).getTextContent();
                     String itemQuantity = invElement.getElementsByTagName("Quantity").item(0).getTextContent();
                     inventoryItems.put(itemName, Integer.parseInt(itemQuantity));                                          
                 }
                 Inventory inv = new Inventory(inventoryItems);
                 ScheduleImpl sched = new ScheduleImpl(Integer.parseInt(facilityRate));
                 HashMap<String,Integer> neighbors = new HashMap<String,Integer>();
                 NodeList neighborsList = elem.getElementsByTagName("Link");
                 for (int j =0; j < neighborsList.getLength(); j++)
                 {
                	 if (neighborsList.item(j).getNodeType() == Node.TEXT_NODE) {
                         continue;
                     }

                     entryName = neighborsList.item(j).getNodeName();
                     if (!entryName.equals("Link")) {
                         System.err.println("Unexpected node found: " + entryName);
                         break;
                     }
                     
                     elem = (Element) neighborsList.item(j);
                     String name = elem.getElementsByTagName("Name").item(0).getTextContent();
                     String distance = elem.getElementsByTagName("Distance").item(0).getTextContent();
                 	 
                     neighbors.put(name, Integer.parseInt(distance));
                 }
                 int cost = Integer.parseInt(facilityCost);
                 int rate = Integer.parseInt(facilityRate);
                 
                 Facility f=null;
				try {
					f = FacilityFactory.createFacility(facilityName, cost, rate, inv, sched, neighbors);
				} catch (FacilityInvalidDataException e) {
					System.out.println("Invalid data for facility, " + e.getMessage());
				}
				if (f != null)
                 facilities.add(f);
            }    
		}		
		catch (ParserConfigurationException | SAXException | IOException | DOMException e) 
		{			
			System.out.println("Error occured in parsing xml file.");
			e.printStackTrace();		  
			System.exit(-1);
		} 
		return facilities;		
	}
	
}
