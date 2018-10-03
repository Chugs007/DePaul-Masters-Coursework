package logisticsproject.facility;

import java.util.*;

import logisticsproject.exceptions.FacilityInvalidDataException;
import logisticsproject.exceptions.FacilityNeighborDoesNotExistException;
import logisticsproject.exceptions.InvalidDayArgumentException;
import logisticsproject.exceptions.ItemDoesNotExistException;
import logisticsproject.exceptions.ItemQuantityIllegalArgumentException;

public interface Facility {
	public final int HOURSPERDAY = 8;
	public final int MILESPERHOUR = 50;
	//Gets a list of neighbors for a given facility.
	public ArrayList<String> GetNeighbors();
	//Gets the distance to specified facility. Throws error if facility, f, is not a neighbor
	//of given facility.
	public int GetDistance(Facility f) throws FacilityNeighborDoesNotExistException;
	//updates the quantity of a certain item by subtracting given value
	//can throw error if amount is more than current value or if item does not 
	//exists in inventory of facility.
	public void UpdateItem(String name, int value) throws ItemDoesNotExistException, ItemQuantityIllegalArgumentException;
	//Gets the quantity of specified item from facility
	//can throw error if item does not exist in inventory.
	public int GetItemQuantity(String name);
	//processes item at start day for amount specified amount.
	//can throw error if day is < 1.
	public void ProcessItems(int startDay, int amount) throws InvalidDayArgumentException;
	//gets next available day for processing from schedule, starting at specified start day.
	public int GetNextAvailableDay();
	//Gets the last day of processing items.
	public int GetProcessingEndDay(int itemQuantity,int startDay);
	//Prints the facility status report
	public void printReport();
	//Checks to see if item exists in facility's inventory.
	public boolean itemExists(String itemName);
	//return the facility name.
	public String GetFacilityName();
	//returns the facility rate.
	public int GetFacilityRate();
	//return the facility cost.
	public int GetFacilityCost();
	//Gets the facility processing cost.
	public double GetProcessingCost(int itemsToProcess,int startDay);
}
