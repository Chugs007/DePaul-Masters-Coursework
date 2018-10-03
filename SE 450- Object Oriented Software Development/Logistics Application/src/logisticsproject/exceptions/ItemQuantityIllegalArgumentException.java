package logisticsproject.exceptions;

//Exception class used for when updating a facility's item quantity, and given amount to subtract 
//from current value results in Item Quantity being < 0.
public class ItemQuantityIllegalArgumentException extends Exception{
	public ItemQuantityIllegalArgumentException()
	{
		super();
	}
	
	public ItemQuantityIllegalArgumentException(String message)
	{
		super(message);
	}
	
}
