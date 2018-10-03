package logisticsproject.exceptions;

public class OrderInvalidDataException extends Exception {

	public OrderInvalidDataException()
	{
		super();
	}
	
	public OrderInvalidDataException(String message)
	{
		super(message);
	}
}
