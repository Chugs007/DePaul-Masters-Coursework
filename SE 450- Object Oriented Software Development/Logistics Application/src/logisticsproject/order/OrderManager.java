package logisticsproject.order;
import java.util.*;

import logisticsproject.exceptions.OrderDoesNotExistException;

public class OrderManager {

	private HashMap<String,Order> ordersCollection;
	private volatile static OrderManager instance;	
	
	private OrderManager()
	{
		ordersCollection = new HashMap<String,Order>();
		ArrayList<Order> orders = OrderXMLLoader.LoadXMLFile();
		for (Order o : orders)
			ordersCollection.put(o.GetOrderID(), o);
	}
	
	public static OrderManager getInstance()
	{
		if (instance == null)
		{
			synchronized (OrderManager.class)
			{
				if (instance == null)
					instance = new OrderManager();
			}			
		}	
		return instance;
	}
	
	public void printOrderReport()
	{		
		for(Order o : ordersCollection.values())
			o.printOrderReport();
	}
	
	 
	public int GetOrderTime(String OrderID) throws OrderDoesNotExistException
	{
		Order order = ordersCollection.get(OrderID);
		if (order == null)
			throw new OrderDoesNotExistException("Order with specified id: " + OrderID + " does not exist.");
		return order.GetOrderTime();
	}
	
	public String getOrderDestination(String OrderID) throws OrderDoesNotExistException
	{
		Order order = ordersCollection.get(OrderID);
		if (order == null)
			throw new OrderDoesNotExistException("Order with specified id: " + OrderID + " does not exist.");
		return order.GetOrderDestination();		
	}
	
	public ArrayList<OrderItem> GetOrderItems(String OrderID) throws OrderDoesNotExistException
	{
		Order order = ordersCollection.get(OrderID);
		if ( order == null)
			throw new OrderDoesNotExistException("Order with specified id: " + OrderID + " does not exist.");
		return order.GetOrderItems();
	}
	
	public void printProcessingSolution()
	{
		
	}
	
	
	
}
