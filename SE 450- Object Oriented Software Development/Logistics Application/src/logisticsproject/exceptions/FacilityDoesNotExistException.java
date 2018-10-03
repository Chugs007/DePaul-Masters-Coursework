package logisticsproject.exceptions;

//Exception class used for when a specified facility given by its name does not exist in the list of facilities
//managed by the facility manager.
public class FacilityDoesNotExistException extends Exception {

	
	public FacilityDoesNotExistException()
	{
		super();
	}
	
	public FacilityDoesNotExistException(String message)
	{
		super(message);
	}
}
