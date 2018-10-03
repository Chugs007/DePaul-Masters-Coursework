package logisticsproject.facility;
import java.util.*;
import logisticsproject.exceptions.*;

public class ScheduleImpl implements Schedule{
	private HashMap<Integer,Integer> dayschedule;
	private int availableItems;  //set to how much a facility can process in a day		
	
	public ScheduleImpl(int fAvailableItems)
	{
		dayschedule = new HashMap<Integer,Integer>();
		availableItems = fAvailableItems;					
	}

	@Override
	//processes items starting at start day for a facility.
	//if start day is full, keep going until free one found.
	//check start day is > 0 otherwise throw error
	//itemsToProcess must also be > 0 otherwise throw error.
	public void ProcessItems(int startDay, int itemsToProcess)throws InvalidDayArgumentException {
		// TODO Auto-generated method stub
		if ( startDay < 1)
			throw new InvalidDayArgumentException();
		int start = startDay;		
		while (itemsToProcess != 0)
		{
			int itemProcessingAvailable = dayschedule.get(start) == null ? availableItems : dayschedule.get(start);
			if (itemsToProcess < itemProcessingAvailable)
			{
				int itemsLeft = itemProcessingAvailable - itemsToProcess;
				itemsToProcess = 0;
				dayschedule.put(start, itemsLeft);				
				return;
			}
			else
			{	
				dayschedule.put(start, 0);
				itemsToProcess -= itemProcessingAvailable;
			}
			start++;			
		}	
	}
		
	@Override
	//Returns the next available day from schedule for processing items.
	//no errors possible here
	public int GetNextAvailableDay() {
		
		int currentDay = 1;
		Object currentValue = dayschedule.get(currentDay);
		while (currentValue != null && (int)currentValue == 0)
		{
			currentDay++;
			currentValue = dayschedule.get(currentDay);
		}		
		return currentDay;
	}

	//Prints the facility's schedule, giving the amount available for processing
	//on each day.
	@Override
	public void printSchedule() {
		// TODO Auto-generated method stub
		System.out.println("Schedule:");
		System.out.printf("%-15s","Day:");
		dayschedule.forEach((k,v) -> System.out.printf("%-5s", k));
		System.out.printf("\n%-15s","Available:");
		dayschedule.forEach((k,v) -> System.out.printf("%-5s",v));
		System.out.println();
	}

	@Override
	public int GetProcessingEndDay(int itemQuantity,int sDay) {
		// TODO Auto-generated method stub		
		int currentDay = sDay;
		while (itemQuantity > 0)  //keep doing until quantity is not > 0.
		{
			//amount to process is either availableItems or whatever is in hashmap entry value.
			int processingAvailable = dayschedule.get(currentDay) == null ? availableItems : dayschedule.get(currentDay);
			itemQuantity -= processingAvailable;
			if (itemQuantity <= 0)
				return currentDay;
			currentDay++;
		}	
		return currentDay;
	}

	@Override
	public double processingCost(int itemsToProcess,int facilityCost,int sDay) {
		// TODO Auto-generated method stub
		int start = sDay;
		double totalCost = 0;
		while (itemsToProcess != 0)	
		{			
			int availableItemsToProcess = dayschedule.get(start) == null ? availableItems : dayschedule.get(start);
			if (itemsToProcess < availableItemsToProcess)
			{				
				double adjustedPercentage = ((double)itemsToProcess/availableItemsToProcess *10000d)/10000d;		
				totalCost += facilityCost*adjustedPercentage;
				itemsToProcess =0;
			}
			else
			{
				itemsToProcess -= availableItemsToProcess;
				totalCost +=   facilityCost;
			}			
			start++;
		}
		return totalCost;	
	}
}
