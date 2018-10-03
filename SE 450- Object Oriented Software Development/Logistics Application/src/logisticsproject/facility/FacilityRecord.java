package logisticsproject.facility;

public class FacilityRecord implements Comparable<FacilityRecord> {
	public String Name;
	public int Quantity;	
	public int ProcessingEndDay;
	public double TravelTime;
	public int ArrivalDay;
	public double Cost;	
	
	public FacilityRecord(String facilityName, int quantity, int processingEndDay, double travelTime, int arrivalDay,double cost)
	{
		Name = facilityName;
		Quantity = quantity;
		ProcessingEndDay = processingEndDay;
		TravelTime = travelTime;
		ArrivalDay= arrivalDay;
		Cost  = cost;		
	}

	@Override
	public int compareTo(FacilityRecord o) {
		// TODO Auto-generated method stub
		FacilityRecord fr = o;
		if (this.ArrivalDay < o.ArrivalDay)
			return -1;
		else if (this.ArrivalDay > o.ArrivalDay)
			return 1;
		else
			return 0;		
	}
}
