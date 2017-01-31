package io.weatherTest;

import static org.junit.Assert.*;

import java.net.URI;
import io.github.ideaqe.weather.WeatherServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.jersey.servlet.ServletContainer;
//import org.glassfish.jersey.client.*;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;
import io.github.ideaqe.weather.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class UnitTest2 {


	Client client;
	
		
    @Before
    public void startServer() throws Exception {
    
         client=ClientBuilder.newClient();
    }

    @Test
    public void testTempsInRange() throws Exception {
    	  	
    	JSONObject timeRanges=new JSONObject();

    	String response= (String) client.target("http://localhost:8080").path("/weather/10/tempRanges")
    			.queryParam("from", "2016-11-20").queryParam("to","2016-12-20").request()
    			.header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get(String.class);
    	System.out.println("HERE is "+response);
    	JSONObject json=getJSONObjForTemps(response);
    	double avg= (double) json.get("avg");
    	double min= (double) json.get("min");
    	double max= (double) json.get("max");
    	assertEquals(avg, 73.0,0);
    }
    
    @Test
    public void testTempsOutOfRange() throws Exception {
    	  	
    	JSONObject timeRanges=new JSONObject();

    	String response= (String) client.target("http://localhost:8080").path("/weather/2/tempRanges")
    			.queryParam("from", "2016-11-28").queryParam("to","2016-12-20").request()
    			.header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get(String.class);
    	System.out.println("HERE shouble be NOT response"+response);
    	JSONObject json=getJSONObjForTemps(response);
    	
    	
    }
    
    @Test
    public void testTempsAtRangeStartDate() throws Exception {
    	  	
    	JSONObject timeRanges=new JSONObject();

    	String response= (String) client.target("http://localhost:8080").path("/weather/2/tempRanges")
    			.queryParam("from", "2016-11-20").queryParam("to","2016-12-20").request()
    			.header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get(String.class);
    	System.out.println("HERE is "+response);
    	JSONObject json=getJSONObjForTemps(response);
    	double avg= (double) json.get("avg");
    	double min= (double) json.get("min");
    	double max= (double) json.get("max");
    	assertEquals(avg, 75.0,0);
    }
    
    @Test
    public void testNotFoundStation() throws Exception{
    	
    	Response response= (Response) client.target("http://localhost:8080").path("/weather/20/tempRanges")
    			.request().header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get();
    	System.out.println("Status is "+response.getStatus());
    }
    
    @Test
    public void testBadRequest() throws Exception{
    	
    	Response response= (Response) client.target("http://localhost:8080").path("/weather/10/tempRanges")
    			.queryParam("from", "2016-111-20").queryParam("to","2016-112-20").request()
    			.header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get();
    	System.out.println("Status is "+response.getStatus());
    }
    
   

    @After 
    public void stopServer() throws Exception {
            client.close();
            
    }
    
	private JSONObject getJSONObjForTemps(String response) throws ParseException {
		JSONParser parser = new JSONParser();
    	JSONObject json = (JSONObject) parser.parse(response);
    	return json;
	}

}
