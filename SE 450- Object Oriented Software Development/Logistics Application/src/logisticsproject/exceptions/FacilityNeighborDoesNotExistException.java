package logisticsproject.exceptions;

//Exception class is used for errors when finding the distance between 2 facilities and they are not neighbors with
//each other.  
public class FacilityNeighborDoesNotExistException extends Exception {

	
	public FacilityNeighborDoesNotExistException()
	{
		super();
	}
	
	public FacilityNeighborDoesNotExistException(String message)
	{
		super(message);
	}
}
