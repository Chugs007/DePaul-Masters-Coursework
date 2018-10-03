package logisticsproject.facility;

import logisticsproject.exceptions.*;

import java.util.*;

public class StandardFacilityImpl implements Facility {
	private int rate;
	private String name;
	private int cost;
	private HashMap<String,Integer> neighbors;
	private Inventory inventory;
	private Schedule schedule;
	
	//constructor sets up facility w/ specified values.
	public StandardFacilityImpl(int frate, String fname, int fcost, HashMap<String,Integer> fneighbors, Inventory fInventory,Schedule fschedule) throws FacilityInvalidDataException
	{
		//validate values for facility name, rate, and cost, If invalid throw exception, ensures that facility object
		//will have valid values for each attribute.
		try {
			setRate(frate);
			setName(fname);
			setCost(fcost);
		} catch (FacilityInvalidDataException e) {
			// TODO Auto-generated catch block
			throw new FacilityInvalidDataException(e.getMessage());
		}			
		neighbors = fneighbors;
		inventory = fInventory;
		schedule = fschedule;
	}
	
	private void setRate(int frate) throws FacilityInvalidDataException
	{
		if (frate <= 0)
			throw new FacilityInvalidDataException();
		rate = frate;
	}
	
	private void setName(String fname) throws FacilityInvalidDataException
	{
		if (fname == null || fname.equals(""))
			throw new FacilityInvalidDataException();
		name = fname;
	}
	
	private void setCost(int fcost) throws FacilityInvalidDataException
	{
		if (fcost <= 0)
			throw new FacilityInvalidDataException();
		cost = fcost;
	}
	
	@Override
	//prints a report w/ various attributes of a facility printed out.
	public void printReport() {
		// TODO Auto-generated method stub
		System.out.println("Facility Status Report");
		System.out.println("----------------------------------------");
		System.out.println(this.name);
		System.out.println("--------");
		System.out.println();
		System.out.println("Rate per day: " + this.rate);
		System.out.printf("Cost per day: $%s",this.cost);
		System.out.println();
		System.out.println();
		System.out.print("Direct Links: ");
		System.out.println();	
		neighbors.forEach((key,value) -> System.out.printf("%s (%.1fd);",key,((double)value)/(Facility.HOURSPERDAY * Facility.MILESPERHOUR)));
		System.out.println();
		System.out.println();
		inventory.printInventory();
		schedule.printSchedule();
		System.out.println("----------------------------------------");
		
	}
	
	//Returns list of neighbors denoted by string name.	
	@Override
	public ArrayList<String> GetNeighbors() {
		// TODO Auto-generated method stub
		Set<String> keySet = neighbors.keySet();
		return new ArrayList<String>(keySet);
	}

	//updates the item in facility's inventory by subtracting specified value.
	@Override
	public void UpdateItem(String name, int value) throws ItemDoesNotExistException, ItemQuantityIllegalArgumentException {
		// TODO Auto-generated method stub
		inventory.updateQuantity(name, value);
	}

	//Gets the amount of an item from a facilty's inventory.
	@Override
	public int GetItemQuantity(String name)  {
		// TODO Auto-generated method stub
		int quant = inventory.getItemQuantity(name);
		return quant;
	}

	//Gets the distance from facility specified.
	@Override
	public int GetDistance(Facility f) throws FacilityNeighborDoesNotExistException {
		// TODO Auto-generated method stub		
		Integer distance = neighbors.get(f.GetFacilityName());
		if (distance == null)
			throw new FacilityNeighborDoesNotExistException();
		return distance;
		
	}

	@Override
	//calls schedule's ProcessItems method to process given amount of items per day,
	//starting at start day.
	public void ProcessItems(int startDay, int amount) throws InvalidDayArgumentException {
		// TODO Auto-generated method stub
		schedule.ProcessItems(startDay, amount);
	}

	@Override
	//calls schedule's GetNextAvailableDay to get the next free day for processing
	//for this facility.
	public int GetNextAvailableDay() {
		// TODO Auto-generated method stub
		return schedule.GetNextAvailableDay();
	}
	
	@Override
	//gets the facility name
	public String GetFacilityName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	//gets the facility rate.
	public int GetFacilityRate() {
		// TODO Auto-generated method stub
		return rate;
	}

	@Override
	//gets the facility cost to process.
	public int GetFacilityCost() {
		// TODO Auto-generated method stub
		return cost;
	}

	@Override
	public boolean itemExists(String itemName) {
		// TODO Auto-generated method stub
		return inventory.ItemExists(itemName);
	}

	@Override
	public int GetProcessingEndDay(int itemQuantity,int sDay) {
		// TODO Auto-generated method stub
		return schedule.GetProcessingEndDay(itemQuantity,sDay);
	}

	public double GetProcessingCost(int itemsToProcess,int sDay)
	{
		return schedule.processingCost(itemsToProcess,this.GetFacilityCost(),sDay);
	}
}
