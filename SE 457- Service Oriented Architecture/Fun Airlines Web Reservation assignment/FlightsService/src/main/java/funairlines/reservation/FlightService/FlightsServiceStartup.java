package funairlines.reservation.FlightService;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

//Class responsible to starting server to run FlightsService on port 8000 at specified address given below.
public class FlightsServiceStartup {

    public static void main(String[] args)
    {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(FlightsService.class);
        sf.setResourceProvider(FlightsService.class,
                new SingletonResourceProvider(new FlightsService()));
        sf.setAddress("http://localhost:8000/reservation/");
        Server server = sf.create();
    }
}
