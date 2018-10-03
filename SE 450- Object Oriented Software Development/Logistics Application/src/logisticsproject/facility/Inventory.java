package logisticsproject.facility;
import java.util.*;

import logisticsproject.exceptions.*;

public class Inventory {
	//list of item names and corresponding quantities.
	private HashMap <String,Integer> itemsList;
	//constructor sets up inventory items list for a facility.
	public Inventory(HashMap<String,Integer> items)
	{
		itemsList = items;
	}
	
	//checks to see if a given item exists in facility inventory.
	public boolean ItemExists(String itemName)
	{
		return itemsList.get(itemName) != null ? true : false;	
	}
	
	//updates the quantity for specified item by subtracting specified value
	//from current quantity of item.  check item exists in catalog before calling.
	//if item does not exist in inventory, throw error, or if amount subtracted is more
	//than amount currently in, throw error also.
	public void updateQuantity(String name,int value) throws ItemDoesNotExistException, ItemQuantityIllegalArgumentException 
	{		
		boolean itemExists = itemsList.containsKey(name);
		if (itemExists)
		{			
			int amount = itemsList.get(name) - value;
			if (amount < 0)
				throw new ItemQuantityIllegalArgumentException();
			itemsList.put(name, amount);
		}
		else
			throw new ItemDoesNotExistException();
	}
	
	//returns the quantity of specified item.
	//Can throw error if item does not exist in item catalog.
	public int getItemQuantity(String name)
	{
		int quantity = -1;
		boolean itemExists = itemsList.containsKey(name);
		if (itemExists)
			quantity = itemsList.get(name);
		return quantity;				
	}
		
	//prints all items in inventory, shows any depleted inventory if any.
	public void printInventory()
	{
		System.out.println("Active Inventory");
		System.out.printf("\t%-10s %-10s","Item ID","Quantity");
		System.out.println();
		itemsList.forEach((key,value) -> System.out.printf("\t%-10s %-10s\n",key,value));
		System.out.println();
		boolean usedUpItem = itemsList.containsValue(0);  //tests to see if any item in list is used up
		if (!usedUpItem)  //no items have been used up
			System.out.println("Depleted (Used-Up) Inventory: None\n");
		//add else block with item/s that are depleted.
	}
}
