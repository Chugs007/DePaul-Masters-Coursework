
package logisticsproject;
import logisticsproject.facility.*;
import logisticsproject.item.ItemManager;
import logisticsproject.order.OrderManager;
import logisticsproject.order.OrderProcessor;
import logisticsproject.shortestpath.ShortestPathProcessor;

public class LogisticsApplication {
	
	
	public static void main(String[] args)	
	{
		/*FacilityManager.getInstance().printFacilityReport();
		ItemManager.getInstance().printItemCatalog();
		System.out.println("Shortest Path Tests:");
		System.out.println();
		ShortestPathProcessor.getInstance().findBestPath("Sante Fe, NM", "Chicago, IL");
		ShortestPathProcessor.getInstance().findBestPath("Atlanta, GA","St. Louis, MO");
		ShortestPathProcessor.getInstance().findBestPath("Seattle, WA","Nashville, TN");
		ShortestPathProcessor.getInstance().findBestPath("New York City, NY","Phoenix, AZ");
		ShortestPathProcessor.getInstance().findBestPath("Fargo, ND","Austin, TX");
		ShortestPathProcessor.getInstance().findBestPath("Denver, CO","Miami, FL");
		ShortestPathProcessor.getInstance().findBestPath("Austin, TX","Norfolk, VA");
		ShortestPathProcessor.getInstance().findBestPath("Miami, FL","Seattle, WA");
		ShortestPathProcessor.getInstance().findBestPath("Los Angeles, CA","Chicago, IL");
		ShortestPathProcessor.getInstance().findBestPath("Detroit, MI","Nashville, TN");
*/		
		
		OrderManager.getInstance().printOrderReport();
		OrderProcessor.GetInstance().ProcessOrder("TO-001");
		OrderProcessor.GetInstance().ProcessOrder("TO-002");
		OrderProcessor.GetInstance().ProcessOrder("TO-003");
		OrderProcessor.GetInstance().ProcessOrder("TO-004");
		OrderProcessor.GetInstance().ProcessOrder("TO-005");
		OrderProcessor.GetInstance().ProcessOrder("TO-006");
		
		}
}
