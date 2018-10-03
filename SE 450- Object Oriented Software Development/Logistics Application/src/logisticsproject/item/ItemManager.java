package logisticsproject.item;
import java.util.*;
import logisticsproject.exceptions.ItemDoesNotExistException;

import logisticsproject.facility.FacilityManager;

public class ItemManager {
	private HashMap<String,Double> itemcatalog;
	private volatile static ItemManager instance;
	
	//Constructor initializes itemcatalog collection, calls the ItemXMLLoader
	//LoadXMLFile method to get Item data and create a list to populate 
	//itemcatalog collection with.
	private ItemManager()
	{
		itemcatalog = new HashMap<String,Double>();
		ArrayList<Item> items = ItemXMLLoader.LoadXMLFile();
		for(Item i : items)
			itemcatalog.put(i.GetItemName(), i.GetItemPrice());			
	}
	
	//returns single instance of ItemManager. Calls private constructor if
	//instance has not yet been initialized.
	public static ItemManager getInstance()
	{
		
		if (instance == null)
		{
			synchronized (ItemManager.class)
			{
				if (instance == null)
					instance = new ItemManager();
			}
		}	
		return instance;
	}
	
	//Searches item catalog collection to see if item is part of catalog.
	public boolean ItemExists(String name)
	{
		return itemcatalog.containsKey(name);
	}
	
	//Gets the item price associated with item name, checks if item exists
	//before attempting to get price.
	public double GetItemPrice(String name) throws ItemDoesNotExistException
	{
		boolean itemExists= ItemExists(name);
		if (!itemExists)
			throw new ItemDoesNotExistException("Specified item," + name + ", does not exist.");
		else
			return itemcatalog.get(name);
		
	}
	
	//Prints out the item catalog using the itemcatalog collection.
	public void printItemCatalog()
	{
		System.out.println("----------------------------------------");
		System.out.println("Item Catalog: ");
		System.out.println();
		int index=0;				
		for(Map.Entry<String,Double> e : itemcatalog.entrySet())
		{
			index++;
			System.out.printf("%-8s : $%-6.0f  ",e.getKey(),e.getValue());
			if (index % 4 == 0)
				System.out.println();
		}
		System.out.println("----------------------------------------");
	}
}
