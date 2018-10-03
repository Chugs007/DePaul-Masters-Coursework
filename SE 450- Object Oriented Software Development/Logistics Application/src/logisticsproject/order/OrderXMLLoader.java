package logisticsproject.order;
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

import logisticsproject.exceptions.OrderInvalidDataException;
import logisticsproject.item.Item;
import logisticsproject.item.ItemFactory;

public class OrderXMLLoader {
	private static final String OrderXmlFilePath = "/src/logisticsproject/order/Orders.xml";
	
	public static ArrayList<Order> LoadXMLFile()
	{
		ArrayList<Order> orders = new ArrayList<Order>();
		try
		{
			String currentDir = System.getProperty("user.dir");
			String fileName = currentDir + OrderXmlFilePath;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			File xml = new File(fileName);
			if (!xml.exists())
			{
				System.err.println("**** XML File '" + fileName + "' cannot be found");
				System.exit(-1);
			}
			
			Document doc = db.parse(xml);
			doc.getDocumentElement().normalize();
			
			NodeList orderEntries = doc.getDocumentElement().getChildNodes();
			for (int i=0; i < orderEntries.getLength(); i++)
			{
				if (orderEntries.item(i).getNodeType() == Node.TEXT_NODE)
				{
					continue;
				}
				
				String entryName = orderEntries.item(i).getNodeName();
				if (!entryName.equals("Order"))
				{
					System.err.println("Unexpected node found: " + entryName);
                    break;
				}
				
				Element elem = (Element) orderEntries.item(i);
				String orderID = elem.getElementsByTagName("OrderID").item(0).getTextContent();
				String orderTime = elem.getElementsByTagName("OrderTime").item(0).getTextContent();
				int oTime = Integer.parseInt(orderTime);
				String orderDestination = elem.getElementsByTagName("Destination").item(0).getTextContent();
				//HashMap<String, Integer> orderItems = new HashMap<String, Integer>();
				ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();
				Element orderItemsElement = (Element)elem.getElementsByTagName("Items").item(0);
				NodeList orderItemList = orderItemsElement.getElementsByTagName("Item");
				for (int j=0; j < orderItemList.getLength(); j++)
				{
					if (orderItemList.item(j).getNodeType() == Node.TEXT_NODE){
						continue;
					}
					
					Element orderItemElement = (Element) orderItemList.item(j);
					String itemName = orderItemElement.getElementsByTagName("Name").item(0).getTextContent();
					String itemQuantity = orderItemElement.getElementsByTagName("Quantity").item(0).getTextContent();
					int itemQuant = Integer.parseInt(itemQuantity);
					//orderItems.put(itemName,itemQuant);
					orderItems.add(new OrderItem(itemName,itemQuant));
				}
				
				Order o = null;
				try {
					o = OrderFactory.createOrder(orderID, oTime, orderDestination, orderItems);
				} catch (OrderInvalidDataException e) {
					// TODO Auto-generated catch block
					System.out.println("Invalid data for order, " + e.getMessage());
				}				
				if (o != null)
					orders.add(o);							
			}
		}
		catch (ParserConfigurationException | SAXException | IOException | DOMException  e)
		{
			System.out.println("Error occured in parsing xml file.");
			e.printStackTrace();		  
			System.exit(-1); //terminate application here, since unable to parse xml file.   
		}
		return orders;
	}
}
