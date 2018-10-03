package logisticsproject.facility;

import java.util.*;

import logisticsproject.exceptions.FacilityInvalidDataException;

public class FacilityFactory {

	//private constructor, no need to create instance of factory.
	private FacilityFactory()
	{
		
	}
	
	//Static method used to create a implementation of the Facility interface.
	//Currently, only standard facility is created but can be further expanded
	//in the future to accommodate new Facility type.
	public static Facility createFacility(String name, int cost, int rate, Inventory i, Schedule s, HashMap<String,Integer> neighbors) throws FacilityInvalidDataException 
	{
		return new StandardFacilityImpl(rate, name, cost, neighbors, i, s);			
	}
}
