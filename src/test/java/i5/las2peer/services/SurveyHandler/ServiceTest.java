package i5.las2peer.services.SurveyHandler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
public class ServiceTest {


	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgentImpl testAgent;
	private static final String testPass = "adamspass";

	private static final String mainPath = "SurveyHandler/";

	/**
	 * Called before a test starts.
	 * <p>
	 * Sets up the node, initializes connector and adds user agent that can be used throughout the test.
	 *
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {
		// start node
		node = new LocalNodeManager().newNode();
		node.launch();

		// add agent to node
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agents must be unlocked in order to be stored
		node.storeAgent(testAgent);

		//my testingagent
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agents must be unlocked in order to be stored
		node.storeAgent(testAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(SurveyHandlerService.class.getName(), "1.0.0"), "a pass");

		// start connector
		connector = new WebConnector(true, 0, false, 0); // port 0 means use system defined port

		logStream = new ByteArrayOutputStream();
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
	}

	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 *
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}

	/**
	 * Tests the validation method.
	 */
	@Test
	public void testGet() {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);

			ClientResponse result = client.sendRequest("GET", mainPath + "get", "");
			Assert.assertEquals(200, result.getHttpCode());
			Assert.assertEquals("adam", result.getResponse().trim());// YOUR RESULT VALUE HERE
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	/**
	 * Test the example method that consumes one path parameter which we give the value "testInput" in this test.
	 */
	@Test
	public void testPost() {
		try {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(testAgent.getIdentifier(), testPass);

			// testInput is the pathParam
			ClientResponse result = client.sendRequest("POST", mainPath + "post/testInput", "");
			Assert.assertEquals(200, result.getHttpCode());
			// "testInput" name is part of response
			Assert.assertTrue(result.getResponse().trim().contains("testInput"));
			System.out.println("Result of 'testPost': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}


	@Test
	public void testSessionKey() {
		String username = "";
		String password = "";
		String uri = "https://NAME.limequery.com/admin/remotecontrol";
		String url= "https://limesurvey.tech4comp.dbis.rwth-aachen.de/";
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"method\": \"get_session_key\", \"params\": [ \""+username+"\", \"" +password+"\"], \"id\": 1}"))
				.build();
		try{
			HttpResponse<String> response =
					client.send(request, HttpResponse.BodyHandlers.ofString());
			String body = response.body().toString();
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject bodyJson = (JSONObject) p.parse(body);
			JSONObject res = new JSONObject();
			String sessionKey = bodyJson.getAsString("result");
			String pa = "";
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}


	/*
	@Test
	public void setUpSurvey(){
		//body will be the returned xml file
		String body ="";

		try{
			String jsonString = getJSONString(body);
			JSONObject bodyJSON = getJSONObject(jsonString);
			getSurveyID(bodyJSON);

			org.json.JSONObject e = XML.toJSONObject(body);
			String joString = e.toString(4);
			//String jo = joString.replace("\n", "").replace("\r", "");

			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject bodyJson = (JSONObject) p.parse(joString);
			//String sessionKey = bodyJson.getAsString("result");

			String surveyIDd= bodyJson.getAsString("document");


			//JSONObject testID = (JSONObject) ((JSONObject) bodyJson.get("document")).get("groups");

		}
		catch(Exception e){
			System.out.println(e.toString());
		}

	}
	*/

}
