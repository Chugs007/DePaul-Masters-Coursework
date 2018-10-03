package logisticsproject.exceptions;

//Exception class used for when a integer value given that is < 1, which is not a valid day used for the schedule
//of each facility.
public class InvalidDayArgumentException extends Exception {

	public InvalidDayArgumentException()
	{
		super();
	}
	
	public InvalidDayArgumentException(String message)
	{
		super(message);
	}
}
