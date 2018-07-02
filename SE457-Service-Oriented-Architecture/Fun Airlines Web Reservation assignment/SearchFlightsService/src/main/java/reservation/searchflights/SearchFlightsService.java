package reservation.searchflights;


import funairlines.reservation.Flight.Flight;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


@Path("searchflights")
//SearchFlightsService is a entity service that uses the FlightsService to find flights matching given criteria.
public class SearchFlightsService {
    static final String FLIGHTS_URI = "http://localhost:8000/reservation/flights";

    @GET
    @Path("/{from}/{to}/{date}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    //Gets matching flights with values matching from and to locations, as well as being on the same day as date given as parameter.
    public List<Flight> getMatchingFlights(@PathParam("from") String from, @PathParam("to") String to,@PathParam("date") String date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date depDate = null;
        List<Flight> flights= new ArrayList<Flight>();
        List<Flight> matchingFlights = new ArrayList<Flight>();
        try
        {
            //parse date parameter, then make request to flights service to get list of flights.
            depDate = sdf.parse(date);
            WebClient client = WebClient.create(FLIGHTS_URI);
            client = client.accept("application/xml").path("getFlights/");
            flights = (List<Flight>) client.getCollection(Flight.class);
            //iterate through flights collection and find matching flights.
            for(Flight f : flights)
            {
                if (sameDay(depDate,f.getDeparture()) && from.equals(f.getFromLocation()) && to.equals(f.getDestinationLocation()))
                {
                    matchingFlights.add(f);
                }
            }
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println(matchingFlights.size());
        return matchingFlights;
    }

    //Method checks to see if two dates occur on the same day of a given year.
    private boolean sameDay(Date d1, Date d2) throws Exception {
        if (d1 == null || d2 == null)
            throw new Exception("Invalid dates given");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        if ((cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) && (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)))
            return true;
        else
            return false;

    }


}
