package logisticsproject.facility;
import logisticsproject.exceptions.InvalidDayArgumentException;

public interface Schedule {
	

	//start processing at given start day continuing until 
	//the amount of items to Process has been used up.
	public void ProcessItems(int startDay, int itemsToProcess) throws InvalidDayArgumentException;
	
	//Gets the next available day for processing orders.
	public int GetNextAvailableDay();
	
	//prints the days with the corresponding
	//items available to process.
	public void printSchedule();
	
	//Gets the last day for processing an item.
	public int GetProcessingEndDay(int itemQuantity,int startDay);
	
	//gets the processing cost for an item.
	public double processingCost(int itemsToProcess,int faciilityCost,int startDay);
}
