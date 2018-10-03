package logisticsproject.order;

import java.util.*;


public interface Order {

	//Gets the order ID for specified order.
	public String GetOrderID();
	
	//Gets the order time, returned as a day value.
	public int GetOrderTime();
	
	//Gets the destination for the specified order.
	public String GetOrderDestination();
	
	//Gets a collection of items for specified order.
	public ArrayList<OrderItem> GetOrderItems();
	
	//Prints the attributes of an order.
	public void printOrderReport();
	
}
