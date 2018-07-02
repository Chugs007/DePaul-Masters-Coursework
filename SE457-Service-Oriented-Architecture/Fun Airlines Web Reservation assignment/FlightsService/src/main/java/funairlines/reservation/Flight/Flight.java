package funairlines.reservation.Flight;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "flight")
//Flight class responsible for representing flight entity with all the different attributes pertaining to it.
//Includes flightId,departure, arrival, from and destination location, etc.
public class Flight {

    private int flightId;
    private String fromLocation;
    private String destinationLocation;
    private Date departure;
    private Date arrival;
    private int capacity;
    private FlightStatus flightStatus;
    private double price;

    public Flight()
    {

    }
    public Flight(int flightId,String fromLocation, String destinationLocation, Date departure, Date arrival, int capacity, FlightStatus flightStatus, double price)
    {
        this.flightId = flightId;
        this.fromLocation = fromLocation;
        this.destinationLocation = destinationLocation;
        this.departure = departure;
        this.arrival = arrival;
        this.capacity = capacity;
        this.flightStatus = flightStatus;
        this.price = price;
    }


    public int getFlightId()
    {
        return flightId;
    }

    public void setFlightId(int flightId)
    {
        this.flightId = flightId;
    }

    public String getFromLocation()
    {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation)
    {
        this.fromLocation = fromLocation;
    }

    public String getDestinationLocation()
    {
        return destinationLocation;
    }
    public void setDestinationLocation(String destinationLocation)
    {
        this.destinationLocation = destinationLocation;
    }

    public Date getDeparture()
    {
        return departure;
    }

    public void setDeparture(Date departure)
    {
        this.departure = departure;
    }

    public Date getArrival()
    {
        return arrival;
    }

    public void setArrival(Date arrival)
    {
        this.arrival = arrival;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    public FlightStatus getFlightStatus()
    {
        return flightStatus;
    }


    public void setFlightStatus(FlightStatus flightStatus)
    {
        this.flightStatus = flightStatus;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }


    @Override
    public String toString() {
        return String.format("Flight id: " + flightId + " from: " + fromLocation + " to: " + destinationLocation + " Capacity: " + capacity);
    }
}
