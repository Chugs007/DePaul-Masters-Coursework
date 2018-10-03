package logisticsproject.item;

public class ItemImpl implements Item {
		
	private String name;
	private double price;
	
	public ItemImpl(String itemName, double itemPrice)
	{
		name = itemName;
		price = itemPrice;		
	}
	
	@Override
	//Gets the name of the item.
	public String GetItemName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	@Override
	//Gets the price of the item.
	public double GetItemPrice() {
		// TODO Auto-generated method stub
		return price;
	}
}
