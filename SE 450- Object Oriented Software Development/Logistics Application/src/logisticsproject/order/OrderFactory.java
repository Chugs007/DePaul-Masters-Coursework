package logisticsproject.order;

import java.util.ArrayList;
import java.util.HashMap;

import logisticsproject.exceptions.OrderInvalidDataException;

public class OrderFactory {

	private OrderFactory()
	{
		
	}
	
	public static Order createOrder(String orderID, int orderTime, String orderDestination, ArrayList<OrderItem> orderItems) throws OrderInvalidDataException
	{
		return new StandardOrderImpl(orderID,orderTime,orderDestination, orderItems);
	}
}
