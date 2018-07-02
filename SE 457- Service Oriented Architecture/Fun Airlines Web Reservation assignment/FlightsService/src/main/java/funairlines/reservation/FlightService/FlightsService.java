package funairlines.reservation.FlightService;

import   funairlines.reservation.Flight.Flight;
import   funairlines.reservation.Flight.FlightStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Path("/flights")
public class FlightsService {

    private List<Flight> flights = new ArrayList<Flight>();

    public FlightsService()
    {
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        try
        {
            flights = new ArrayList<Flight>();
            Flight f1 = new Flight(1,"Chicago","Houston",dateformat.parse("08-05-2018 08:00:00"),dateformat.parse("08-05-2018 10:30:00"),75,FlightStatus.ONTIME,140);
            Flight f2 = new Flight(2,"Dallas","Seattle",dateformat.parse("08-05-2018 09:00:00"),dateformat.parse("08-05-2018 11:30:00"),80,FlightStatus.ONTIME,130);
            Flight f3 = new Flight(3,"Chicago","Houston",dateformat.parse("07-05-2017 05:00:00"),dateformat.parse("08-05-2017 07:30:00"),12,FlightStatus.ONTIME,150);
            Flight f4 = new Flight(4,"Chicago","Houston",dateformat.parse("09-05-2018 05:00:00"),dateformat.parse("08-05-2018 07:30:00"),12,FlightStatus.ONTIME,175);
            Flight f5 = new Flight(5,"Des Moines","Denver",dateformat.parse("10-05-2018 06:00:00"),dateformat.parse("08-05-2018 08:30:00"),12,FlightStatus.ONTIME,135);
            Flight f6 = new Flight(6,"Chicago","Los Angeles",dateformat.parse("11-05-2018 05:00:00"),dateformat.parse("08-05-2018 08:40:00"),12,FlightStatus.ONTIME,125);
            Flight f7 = new Flight(7,"Chicago","Houston",dateformat.parse("08-05-2018 13:00:00"),dateformat.parse("08-05-2018 15:30:00"),12,FlightStatus.ONTIME,111);
            flights.add(f1);
            flights.add(f2);
            flights.add(f3);
            flights.add(f4);
            flights.add(f5);
            flights.add(f6);
            flights.add(f7);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/getFlights")
    //Gets all flights currently in list, returns xml.
    public List<Flight> getFlights()
    {
        return flights;
    }


    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getFlights")
    //Gets all flights currently in list, returns json.
    public List<Flight> getFlightsJSON()
    {
        return flights;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{flightId}")
    //Gets flight identified by flightId parameter passed, throws exception if no match found.
    public Flight getFlight(@PathParam("flightId") int flightId) throws Exception {
        Flight flight = null;
        for(Flight f : flights)
        {
            if (f.getFlightId() == flightId) {
                flight = f;
                break;
            }
        }

        if (flight != null)
            return flight;
        else
            throw new Exception("Flight with flight id: " + flightId + " could not be found.");
    }

    @POST
    @Path("")
    //Adds flight to list.
    public void addFlight(Flight f)
    {
        if (!flights.contains(f))
        {
            flights.add(f);
        }
    }


    @DELETE
    @Path("/{flightId}")
    //Deletes flight identified by flightId parameter.
    public void deleteFlight(@PathParam("flightId") int flightId)
    {
        for(Flight f : flights)
        {
            if (f.getFlightId() == flightId) {
               flights.remove(f);
               return;
            }
        }

    }

}
