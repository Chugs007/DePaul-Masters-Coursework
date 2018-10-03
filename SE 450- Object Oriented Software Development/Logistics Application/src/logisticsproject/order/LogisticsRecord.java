package logisticsproject.order;

import java.util.ArrayList;

import logisticsproject.facility.FacilityRecord;

public class LogisticsRecord {
	
	public String ItemName;
	public ArrayList<FacilityRecord> facilityRecords;	
	public double totalCost;
	public int totalQuantity;
	public int MinArrivalDay;
	public int MaxArrivalDay;
	
	public LogisticsRecord(String itemName, ArrayList<FacilityRecord> records, double total, int totalQuantity, int minArrival, int maxArrival)
	{
		this.ItemName = itemName;
		this.facilityRecords = records;
		this.totalCost = total;
		this.totalQuantity = totalQuantity;
		this.MinArrivalDay = minArrival;
		this.MaxArrivalDay = maxArrival;
	}
}
