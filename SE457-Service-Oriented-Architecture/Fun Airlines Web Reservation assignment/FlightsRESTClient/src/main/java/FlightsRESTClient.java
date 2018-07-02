
import org.apache.cxf.jaxrs.client.WebClient;

public class FlightsRESTClient {

    static final String FLIGHTS_URI = "http://localhost:8000/reservation";

    public static void main(String[] args)
    {
        WebClient client = WebClient.create(FLIGHTS_URI);

        client = client.accept("application/xml").path("flights/getFlights");
        String result = client.get(String.class);
        System.out.println(result);
        System.out.println();
        System.out.println();
        System.out.println();
        WebClient client2 = WebClient.create(FLIGHTS_URI);
        client2 = client2.accept("application/xml").path("flights/1");
        String result2 = client2.get(String.class);
        System.out.println(result2);
        WebClient client3 = WebClient.create(FLIGHTS_URI);
        client3 = client3.path("flights/2");
        client3.delete();
        System.out.println("Deleted flight with id 2 from FlightsService");

    }
}
