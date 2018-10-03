package logisticsproject.exceptions;

public class OrderDoesNotExistException extends Exception {

	public OrderDoesNotExistException()
	{
		super();
	}
	
	public OrderDoesNotExistException(String message)
	{
		super(message);
	}
}
