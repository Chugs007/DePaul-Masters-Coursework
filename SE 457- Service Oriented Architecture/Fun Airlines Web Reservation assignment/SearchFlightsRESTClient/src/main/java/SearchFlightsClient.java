import org.apache.cxf.jaxrs.client.WebClient;

public class SearchFlightsClient {

    static final String FLIGHTS_URI = "http://localhost:8001/reservation";

    public static void main(String[] args)
    {
        WebClient client = WebClient.create(FLIGHTS_URI);
        client = client.accept("application/xml").path("searchflights/Chicago/Houston/08%2F05%2F2018%0D%0A");

        String result = client.get(String.class);

        System.out.println(result);
    }

}
