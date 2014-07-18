
package eduroutenamespace;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * 
 * 			Eduroute SOAP interface.
 * 		
 * 
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "EdurouteDistributeurService", targetNamespace = "urn:edurouteNamespace", wsdlLocation = "file:/Users/steven/Downloads/uitgeverAPI.xml")
public class EdurouteDistributeurService
    extends Service
{

    private final static URL EDUROUTEDISTRIBUTEURSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(eduroutenamespace.EdurouteDistributeurService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = eduroutenamespace.EdurouteDistributeurService.class.getResource(".");
            url = new URL(baseUrl, "file:/Users/steven/Downloads/uitgeverAPI.xml");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/Users/steven/Downloads/uitgeverAPI.xml', retrying as a local file");
            logger.warning(e.getMessage());
        }
        EDUROUTEDISTRIBUTEURSERVICE_WSDL_LOCATION = url;
    }

    public EdurouteDistributeurService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EdurouteDistributeurService() {
        super(EDUROUTEDISTRIBUTEURSERVICE_WSDL_LOCATION, new QName("urn:edurouteNamespace", "EdurouteDistributeurService"));
    }

    /**
     * 
     * @return
     *     returns KeyPortType
     */
    @WebEndpoint(name = "MyPort")
    public KeyPortType getMyPort() {
        return super.getPort(new QName("urn:edurouteNamespace", "MyPort"), KeyPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns KeyPortType
     */
    @WebEndpoint(name = "MyPort")
    public KeyPortType getMyPort(WebServiceFeature... features) {
        return super.getPort(new QName("urn:edurouteNamespace", "MyPort"), KeyPortType.class, features);
    }

}
