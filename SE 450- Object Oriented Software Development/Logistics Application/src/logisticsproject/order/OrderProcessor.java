package logisticsproject.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;

import logisticsproject.facility.*;
import logisticsproject.item.Item;
import logisticsproject.item.ItemManager;
import logisticsproject.shortestpath.ShortestPathProcessor;
import logisticsproject.exceptions.FacilityDoesNotExistException;
import logisticsproject.exceptions.InvalidDayArgumentException;
import logisticsproject.exceptions.ItemDoesNotExistException;
import logisticsproject.exceptions.ItemQuantityIllegalArgumentException;
import logisticsproject.exceptions.OrderDoesNotExistException;

public class OrderProcessor {

	private volatile static OrderProcessor instance;
	private ArrayList<FacilityRecord> facilityRecords;
	private ArrayList<LogisticsRecord> logisticsRecord;
	private int orderItemQuantity = 0;
	private final double transportCost = 500;
	
	
	private OrderProcessor()
	{
		facilityRecords = new ArrayList<FacilityRecord>();
		logisticsRecord = new ArrayList<LogisticsRecord>();
	}
	
	public static OrderProcessor GetInstance()
	{
		if (instance == null)
		{
			synchronized(OrderProcessor.class)
			{
				if (instance == null)
					instance = new OrderProcessor();
			}
		}
		return instance;
	}
	
	public void ProcessOrder(String OrderID) 
	{
		logisticsRecord.clear();
		facilityRecords.clear();
		ArrayList<Facility> facilities= null;		
		try {
			ArrayList<OrderItem> orderItems = OrderManager.getInstance().GetOrderItems(OrderID);
			//HashMap<String,Integer> orderItems1 = OrderManager.getInstance().GetOrderItems(OrderID);
			//TreeMap<String,Integer> orderItems = new TreeMap<String,Integer>(orderItems1);
			String destination = OrderManager.getInstance().getOrderDestination(OrderID);
			int orderTime = OrderManager.getInstance().GetOrderTime(OrderID);
			for(OrderItem oi : orderItems)
			{
				String key = oi.getName();
				ArrayList<FacilityRecord> itemFacilityRecords = new ArrayList<FacilityRecord>();
				orderItemQuantity = oi.getQuantity();
				if (!ItemManager.getInstance().ItemExists(key))
					continue;			
				facilities = FacilityManager.getInstance().getFacilitiesContainingItem(key);
				facilities.removeIf(f->f.GetFacilityName().equals(destination));
				if (facilities.isEmpty())
				{
					System.out.println("Item: " + key + " put on back order.");
				}
				do 
				{
					for (Facility f : facilities)
					{					
						FacilityRecord fr = generateFacilityRecord(f,destination,key,orderItemQuantity,orderTime);
						facilityRecords.add(fr);
					}
					Collections.sort(facilityRecords); 				
					FacilityRecord fe = facilityRecords.get(0);					
					processFacilityRecord(fe,key,orderTime);
					facilities.removeIf(f -> f.GetFacilityName().equals(fe.Name));
					itemFacilityRecords.add(fe);
					facilityRecords.clear();					
				}
				while (orderItemQuantity > 0);
				
				
				//generate logistics record here for each order item
				LogisticsRecord lr = generateLogisticsRecord(key,itemFacilityRecords);
				logisticsRecord.add(lr);
				
			}
			
			printOrderReport(OrderID);
		} catch (OrderDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (FacilityDoesNotExistException e)
		{
			 
		}
	}	
	
	private FacilityRecord generateFacilityRecord(Facility f,String destination,String itemName, int orderItemQuantity,int orderTime)
	{
		FacilityRecord fr= null;
		try {
 			int quantityNeeded = Math.min(f.GetItemQuantity(itemName),orderItemQuantity); 			
			double travelTime = ShortestPathProcessor.getInstance().findBestPath(f.GetFacilityName(), destination);			
			int processingEndDay = Math.round(FacilityManager.getInstance().GetFacilityProcessingEndDay(f.GetFacilityName(), quantityNeeded,orderTime));	
			int arrivalDay = (int)Math.ceil(travelTime) + processingEndDay;
			double itemCost =  ItemManager.getInstance().GetItemPrice(itemName) * quantityNeeded; 
			double facilityProcessingCost = FacilityManager.getInstance().GetProcessingCost(f.GetFacilityName(), quantityNeeded,orderTime);
			double transportingCost = Math.ceil(travelTime) * transportCost;
			double cost = itemCost + facilityProcessingCost + transportingCost;
			cost = (double)Math.round(cost * 100) /100;
			fr = FacilityManager.getInstance().GenerateFacilityRecord(f, quantityNeeded,processingEndDay, travelTime, arrivalDay,cost);
							
		} catch (ItemDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return fr;
	}
	
	private LogisticsRecord generateLogisticsRecord(String itemName,ArrayList<FacilityRecord> records)
	{
		int totalQuantity = 0;
		double totalCost = 0;
		int min = records.get(0).ArrivalDay;
		int max = records.get(0).ArrivalDay;
		for(FacilityRecord fe : records)
		{
			totalQuantity += fe.Quantity;
			totalCost += fe.Cost;
			if (fe.ArrivalDay < min)
				min = fe.ArrivalDay;
			if (fe.ArrivalDay > max)
				max = fe.ArrivalDay;
		}
		return new LogisticsRecord(itemName,records,totalCost,totalQuantity,min,max);
	}
	
	private void processFacilityRecord(FacilityRecord fe, String itemName,int startDay) 
	{
		try
		{			
			FacilityManager.getInstance().UpdateFacilityItem(fe.Name,itemName, fe.Quantity);			
			orderItemQuantity -= fe.Quantity;
			FacilityManager.getInstance().ProcessItemsAtFacility(fe.Name, startDay, fe.Quantity);		
		}
		catch (FacilityDoesNotExistException e)
		{
			
		} catch (InvalidDayArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemQuantityIllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void printOrderReport(String OrderID)
	{
		System.out.println("Order: " + OrderID);
		System.out.println();
		//iterate over logistics record collections
		for(LogisticsRecord lr : logisticsRecord)
		{
			System.out.println(lr.ItemName);
			System.out.printf("\t   %-17s %-12s %-12s %-15s\n","Facility","Quantity","Cost","ArrivalDay");
			int index =1;
			for(FacilityRecord fr : lr.facilityRecords)
			{
				System.out.printf("\t" + index + ") %-17s %-12s $%-12s %-15s\n",fr.Name,fr.Quantity,fr.Cost,fr.ArrivalDay);
				index++;
			}
			System.out.printf("\t   %-17s %-12s $%-12s [%d-%d]\n","TOTAL",lr.totalQuantity,lr.totalCost,lr.MinArrivalDay,lr.MaxArrivalDay);
			System.out.println();
		}
		System.out.println("---------------------------------------------------------------");
	}
}
