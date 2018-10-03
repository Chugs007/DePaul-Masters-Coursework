package logisticsproject.item;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import logisticsproject.exceptions.*;

public class ItemXMLLoader {

	private static final String ITEMSXMLFILEPATH = "/src/logisticsproject/item/Items.xml";
	
	//Parses xml file containing information for each item.
	public static ArrayList<Item> LoadXMLFile()
	{
		ArrayList<Item> items = new ArrayList<Item>();
		try
		{
			String currentdir = System.getProperty("user.dir");
			String fileName = currentdir + ITEMSXMLFILEPATH;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File xml = new File(fileName);
            if (!xml.exists()) {
                System.err.println("**** XML File '" + fileName + "' cannot be found");
                System.exit(-1);
            }

            Document doc = db.parse(xml);
            doc.getDocumentElement().normalize();

            NodeList itemEntries = doc.getDocumentElement().getChildNodes();
            for(int i=0; i < itemEntries.getLength(); i++)  //create a facility
            {
            	 if (itemEntries.item(i).getNodeType() == Node.TEXT_NODE) {
                     continue;
                 }
                 
                 String entryName = itemEntries.item(i).getNodeName();
                 if (!entryName.equals("Item")) {
                     System.err.println("Unexpected node found: " + entryName);
                     break;
                 }
                 
                 Element elem = (Element) itemEntries.item(i);
                 String itemName = elem.getElementsByTagName("Name").item(0).getTextContent();
                 String itemPrice = elem.getElementsByTagName("Price").item(0).getTextContent();
                 double price = Double.parseDouble(itemPrice);
                 Item item = ItemFactory.createItem(itemName, price);
                 items.add(item);
            }         
        }
		catch (ParserConfigurationException | SAXException | IOException | DOMException  e)
		{
			System.out.println("Error occured in parsing xml file.");
			e.printStackTrace();		  
			System.exit(-1); //terminate application here, since unable to parse xml file.   
		}
		return items;
	}	
}
