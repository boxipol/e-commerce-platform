import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;
import javax.xml.transform.Source;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebServiceClient
public class HelloServiceIntegrationTest {

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@LocalServerPort
	private int port;

	@Test
	void testHelloEndpoint() {
		String requestXml = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:ex="http://example.com/demo">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <ex:HelloRequest>
                         <ex:name>Petyo</ex:name>
                      </ex:HelloRequest>
                   </soapenv:Body>
                </soapenv:Envelope>
                """;

		Source request = new StringSource(requestXml);
		Source response = webServiceTemplate.sendSourceAndReceive(
			"http://localhost:" + port + "/ws",
			request,
			new SoapActionCallback("http://example.com/demo/HelloRequest")
		);

		assertThat(response).isNotNull();
		// Optionally: assert specific content from the response
	}
}
