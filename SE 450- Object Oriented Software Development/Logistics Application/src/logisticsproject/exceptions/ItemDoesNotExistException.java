package logisticsproject.exceptions;

//Exception class used for when a requested item does not exist in item catalog.
public class ItemDoesNotExistException extends Exception{

	public ItemDoesNotExistException()
	{
		super();
	}
	
	public ItemDoesNotExistException(String message)
	{
		super(message);
	}
}
