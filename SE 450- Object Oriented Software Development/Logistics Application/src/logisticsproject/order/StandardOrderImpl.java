package logisticsproject.order;

import java.util.ArrayList;
import java.util.HashMap;

import logisticsproject.exceptions.OrderInvalidDataException;

public class StandardOrderImpl implements Order {

	private String orderID;
	private int orderTime;
	private String orderDestination;
	private ArrayList<OrderItem> orderItems;
	private static int orderNumber = 1;   // shared variable incremented for each order
	
	public StandardOrderImpl(String oID, int oTime, String oDestination, ArrayList<OrderItem> oItems) throws OrderInvalidDataException	
	{
		try 
		{
			setOrderID(oID);
			setOrderTime(oTime);
			setOrderDestination(oDestination);		
			setOrderItems(oItems);
		}
		catch (OrderInvalidDataException e)
		{
			throw new OrderInvalidDataException(e.getMessage());
		}
	}
	
	private void setOrderID(String oID) throws OrderInvalidDataException
	{
		if (oID == null || oID.equals(""))
			throw new OrderInvalidDataException("Order ID: " + oID + " is not a valid orderID value.");
		orderID = oID;
	}
	
	private void setOrderTime(int oTime) throws OrderInvalidDataException
	{
		if (oTime  < 1 )
			throw new OrderInvalidDataException("Order Time: " + oTime + " is not a valid order time. Must be an integer value > 0.");
		orderTime = oTime;
	}
	
	private void setOrderDestination(String oDestination) throws OrderInvalidDataException	
	{
		if (oDestination == null || oDestination.equals(""))
			throw new OrderInvalidDataException("Order destination: " + oDestination + " is not a valid order destination value.");
		orderDestination = oDestination;
	}
	
	private void setOrderItems(ArrayList<OrderItem> oItems) throws OrderInvalidDataException
	{		
		for(OrderItem oi : oItems)
		{
			String itemName = oi.getName();
			if (itemName == null || itemName == "")
				throw new OrderInvalidDataException();
			int itemQuantity = oi.getQuantity();
				if (itemQuantity <= 0)
					throw new OrderInvalidDataException();
		}
		orderItems = oItems;
	}

	@Override
	public String GetOrderID() {
		// TODO Auto-generated method stub
		return orderID;
	}

	@Override
	public int GetOrderTime() {
		// TODO Auto-generated method stub
		return orderTime;
	}

	@Override
	public String GetOrderDestination() {
		// TODO Auto-generated method stub
		return orderDestination;
	}

	@Override
	public ArrayList<OrderItem> GetOrderItems() {
		// TODO Auto-generated method stub
		return orderItems;
	}

	@Override
	public void printOrderReport() {
		// TODO Auto-generated method stub
		System.out.println("Order #" + orderNumber);
		System.out.println("\tOrder Id: \t" + orderID);
		System.out.println("\tOrder Time: \t" + orderTime);
		System.out.println("\tDestination: \t" + orderDestination);
		System.out.println();
		System.out.println("\tList of Order Items");
		int itemNumber =1;
		
		for (OrderItem oi : orderItems)
		{			
			String itemName = oi.getName();
			int itemQuantity = oi.getQuantity();
			System.out.println("\t" + itemNumber + ") Item ID: \t" + itemName + ",\t Quantity:  " + oi.getQuantity());
			itemNumber++;
		}
		System.out.println();
		orderNumber++;
	}
}
