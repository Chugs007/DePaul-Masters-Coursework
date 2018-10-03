package logisticsproject.shortestpath;
import java.util.*;

import logisticsproject.exceptions.FacilityDoesNotExistException;
import logisticsproject.exceptions.FacilityNeighborDoesNotExistException;
import logisticsproject.facility.FacilityManager;
import logisticsproject.facility.Facility;

public class ShortestPathProcessor {
	private volatile static ShortestPathProcessor instance;
	private HashMap<String,Double> pairs = new HashMap<String,Double>();
	private HashSet<String> seen= new HashSet<String>();
	private ArrayList<String> lowPath= new ArrayList<String>();	
	private double shortestPathdistance; 
		
	private ShortestPathProcessor()
	{
		
	}
		
	//returns a single instance of this class, calls private constructor
	//if not created yet.
	public static ShortestPathProcessor getInstance()
	{
		if (instance == null)
			synchronized (ShortestPathProcessor.class)
			{
				if (instance == null)
					instance = new ShortestPathProcessor();
			}
		return instance;
	}
	
	//Finds the best(shortest) path for given source and destination.
	public double findBestPath(String source, String destination)
	{
		//clear lowPath and seen list first, so each time method called
		//new lists can be created.
		lowPath.clear();   
		seen.clear(); 	
		try 
		{
			//if (pairs.isEmpty())  
				mapPairs(source);
			ArrayList<String> pathList= new ArrayList<String>();
			pathList.add(source);		
			findPaths(source,destination,pathList);
			double lowPathDistance = getDistanceOfPath(lowPath);
			shortestPathdistance =  ((lowPathDistance)/(Facility.HOURSPERDAY * Facility.MILESPERHOUR));
			return shortestPathdistance;
			//printShortestPath(source,destination);
		}
		catch (FacilityDoesNotExistException e)
		{
			System.out.println(e.getMessage());
			return 0;
		}
		catch (FacilityNeighborDoesNotExistException e)
		{
			System.out.println(e.getMessage());
			return 0;
		}				
	}
	
	//Prints output of the shortest path, printing each node in the path with 
	//an arrow indicating direction.  Also shows the distance in miles and the time
	//in days.
	private void printShortestPath(String source, String destination) throws FacilityNeighborDoesNotExistException, FacilityDoesNotExistException
	{
		double lowPathDistance = getDistanceOfPath(lowPath);
		System.out.printf("%s to %s\n",source,destination);
		for (int i = 0; i < lowPath.size(); i++)
		{
			if (i != lowPath.size()-1)
				System.out.print(lowPath.get(i) + " -> ");
			else
				System.out.printf("%s = %.0f mi\n",lowPath.get(i), lowPathDistance);
		}
		shortestPathdistance =  ((lowPathDistance)/(Facility.HOURSPERDAY * Facility.MILESPERHOUR));
		System.out.printf("%.0f / (8 hours per day * 50mph ) = %.2f days\n\n",lowPathDistance,shortestPathdistance);
	}
	 
	//Algorithm used to find shortest path, recursively calling itself to 
	//get the shortest path.
	private void findPaths(String start, String end, ArrayList<String> path) throws FacilityNeighborDoesNotExistException, FacilityDoesNotExistException
	{
		if (start.equals(end))
		{
			if (lowPath.size() == 0)
			{
				for (String p : path)
				{
					lowPath.add(p);
				}
			}				
			else
			{
				double pathDistance = getDistanceOfPath(path);
				double lowPathDistance = getDistanceOfPath(lowPath);
				if (pathDistance < lowPathDistance)
				{
					lowPath.clear();
					for(String p : path)
					{
						lowPath.add(p);
					}
				}
			}
		}
		else
		{
			HashSet<String> fromHere= new HashSet<String>();
			for (String key : pairs.keySet())
			{
				String firstNode = key.split("-")[0];
				if (firstNode.equals(start))
					fromHere.add(key);
			}
			for(String key : fromHere)
			{
				String secondNode = key.split("-")[1];
				if (!path.contains(secondNode))
				{
					ArrayList<String> newPath = new ArrayList<String>();
					for(String p : path)
					{
						newPath.add(p);
					}
					newPath.add(secondNode);
					findPaths(secondNode,end,newPath);
				}
			}
		}
	}
	
	//Gets all the pairs of facilities that are directly connected to each other.
	//Finds pairs by adding the neighbors together w/ the starting facility and recursively calls itself on neighbor if 
	//neighbor hasn't been seen yet.
	private void mapPairs(String init) throws FacilityNeighborDoesNotExistException, FacilityDoesNotExistException 
	{
		seen.add(init);		
		ArrayList<String> neighbors = FacilityManager.getInstance().GetNeighbors(init);
		for (String neighbor: neighbors)
		{
			double distance = FacilityManager.getInstance().GetDistance(init, neighbor);			
			pairs.put(init + "-" + neighbor, distance);
			if (!seen.contains(neighbor))
				mapPairs(neighbor);
		}
	}
	
	//Gets the total distance of a path which is given using an arraylist of strings.
	//It gets the distance from nodes next to each other and accumulates the total distance.
	public double getDistanceOfPath(ArrayList<String> path) throws FacilityNeighborDoesNotExistException, FacilityDoesNotExistException
	{
		//check make sure path list is greater than > 1 else throw error
		double pathDistance=0; 
		for(int i=1; i < path.size(); i++)
		{
			double distance =0;
			distance = FacilityManager.getInstance().GetDistance(path.get(i), path.get(i-1));			
			pathDistance += distance;
		}
		return pathDistance;
	}
	
}
