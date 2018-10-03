package logisticsproject.item;

public class ItemFactory {
	
	private ItemFactory()
	{
		
	}
	
	//Creates an instance of an object who implements the Item interface.
	public static Item createItem(String name, double price)
	{
		return new ItemImpl(name,price);  //default ItemImpl created
	}
	
}
