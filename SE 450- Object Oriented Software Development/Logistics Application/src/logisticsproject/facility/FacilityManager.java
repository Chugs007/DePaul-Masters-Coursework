package logisticsproject.facility;
import java.util.*;

import logisticsproject.exceptions.*;

//Singleton Facade made to provide a layer between clients and the subsystem of facilities.  Used by clients to query for information from facilities 
//regarding inventory, distance, schedule, etc.
public class FacilityManager {
	
	private HashMap<String,Facility> facilities;
	private volatile static FacilityManager instance;
	
	//calls facility xml loader which loads xml file and creates a list
	//of facilities.  
	private FacilityManager()
	{
		facilities = new HashMap<String,Facility>();		
		ArrayList<Facility> flist = FacilityXMLLoader.LoadXMLFile();
		for(Facility f : flist)
			facilities.put(f.GetFacilityName(), f);
	}
	
	//static method creates a singleton instance of FacilityManager.	
	public static FacilityManager getInstance()
	{ 		
		if (instance == null)
		{
			synchronized (FacilityManager.class)
			{
				if (instance == null)
					instance = new FacilityManager();
			}
		}	
		return instance;
	}
	
	//Gets all the neighbors of a specified facility.
	//Can throw error if specified facility does not exist.
	public ArrayList<String> GetNeighbors(String facilityName) throws FacilityDoesNotExistException
	{
		Facility f = facilities.get(facilityName);
		if (f==null)
			throw new FacilityDoesNotExistException("Specified facility" + facilityName + " does not exist.");
		return f.GetNeighbors();
	}
	
	//Gets the distance from a facility to another facility.
	//return 0 in case there is no distance found between the two.
	public int GetDistance(String from, String to) throws FacilityNeighborDoesNotExistException, FacilityDoesNotExistException
	{
		Facility f1 = facilities.get(from);
		if (f1 == null)
			throw new FacilityDoesNotExistException("Specified facility " + from + " does not exist.");
		Facility f2 = facilities.get(to);
		if (f2 == null)
			throw new FacilityDoesNotExistException("Specified facility " + to + " does not exist.");
		return f1.GetDistance(f2);
	}
	
	//updates a specified item at specific facility by subtracting given amount.
	//possible name change to reflect better?(DecreaseFacilityItemQuantity)
	public void UpdateFacilityItem(String facilityName,String itemName, int amount) throws ItemDoesNotExistException, FacilityDoesNotExistException, ItemQuantityIllegalArgumentException
	{
		Facility f = facilities.get(facilityName);
		if (f==null)
			throw new FacilityDoesNotExistException("Specified facility" + facilityName + " does not exist.");
		f.UpdateItem(itemName, amount);
	}
	
	//Gets the current quantity of an item at specified facility.
	//Can throw error if item does not exist in facility or specified facility does not exist.
	public double GetItemQuantity(String facilityName, String itemName)throws ItemDoesNotExistException, FacilityDoesNotExistException
	{
		Facility f = facilities.get(facilityName);
		if (f==null)
			throw new FacilityDoesNotExistException("Specified facility" + facilityName + " does not exist.");
		return f.GetItemQuantity(itemName);
	}
	//processes items at a specified facility starting at given start date.
	//can throw error if day is not > 0 or specified facility does not exist.
	public void ProcessItemsAtFacility(String facilityName, int startDay, int amount) throws InvalidDayArgumentException, FacilityDoesNotExistException
	{
		Facility f = facilities.get(facilityName);
		if (f==null)
			throw new FacilityDoesNotExistException("Specified facility" + facilityName + " does not exist.");
		f.ProcessItems(startDay, amount);
	}
	
	//gets the next available day for processing items at a certain facility.
	//can throw error if specified facility does not exist.
	public int GetFacilityAvailableStartDate(String facilityName) throws FacilityDoesNotExistException
	{
		Facility f= facilities.get(facilityName);
		if (f==null)
			throw new FacilityDoesNotExistException("Specified facility" + facilityName + " does not exist.");
		return f.GetNextAvailableDay();
	}
	
	public ArrayList<Facility> getFacilitiesContainingItem(String itemName) throws FacilityDoesNotExistException
	{
		ArrayList<Facility> facilitiesContainingItem = new ArrayList<Facility>();
		for(Facility f : this.facilities.values())
		{
			if (f.itemExists(itemName)  && f.GetItemQuantity(itemName) != 0)
				facilitiesContainingItem.add(f);
		}
		return facilitiesContainingItem;
	}
	
	public ArrayList<Facility> getFacilities()
	{	
		return (ArrayList<Facility>) facilities.values();
	}
	
	
	public FacilityRecord GenerateFacilityRecord(Facility f,int quantity, int processingEndDay,double travelTime, int arrivalDay, double cost)
	{
		FacilityRecord fr = new FacilityRecord(f.GetFacilityName(),quantity,processingEndDay, travelTime,arrivalDay,cost);
		return fr;
	}
	
	public int GetFacilityProcessingEndDay(String facilityName,int itemQuantity,int sDay)
	{
		Facility f = facilities.get(facilityName);
		return f.GetProcessingEndDay(itemQuantity,sDay);
	}
	//prints each facility's status report.
	public void printFacilityReport()
	{
		for(Facility f : facilities.values())
		{
			f.printReport();
		}
	}
	
	public double GetProcessingCost(String facilityName, int ItemsToProcess,int sday)
	{
		Facility f = facilities.get(facilityName);
		return f.GetProcessingCost(ItemsToProcess,sday);
	}
	
	
}
