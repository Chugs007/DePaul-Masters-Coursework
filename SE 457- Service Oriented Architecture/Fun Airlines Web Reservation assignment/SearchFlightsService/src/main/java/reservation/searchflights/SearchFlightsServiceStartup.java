package reservation.searchflights;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

//Class is responsible for creating server to run SearchFlightsService at given address below.
public class SearchFlightsServiceStartup {

    public static void main(String[] args)
    {

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(SearchFlightsService.class);
        sf.setResourceProvider(SearchFlightsService.class,
                new SingletonResourceProvider(new SearchFlightsService()));
        sf.setAddress("http://localhost:8001/reservation/");
        Server server = sf.create();
    }
}
