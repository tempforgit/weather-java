package io.weatherTest;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import io.github.ideaqe.weather.WeatherServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.client.*;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;
import io.github.ideaqe.weather.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UnitTestAllTemps {

	Client client;
			
    @Before
    public void startServer() throws Exception {

         client=ClientBuilder.newClient();
    }
    @Test
    public void testGetStatus() throws Exception {
    	
    	
    	Response response= client.target("http://localhost:8080").path("/weather/2")
                .request().get();
    	assertEquals(response.getStatus(), 200);
    	
  
    }
    @Test
    public void testGetTemps() throws Exception {
    	final String stationId="2";
    	String responseForAll=  client.target("http://localhost:8080").path("/weather/"+stationId)
                .request().get(String.class);
    	System.out.println("HERE IS "+responseForAll.toString());  
    	String response=  client.target("http://localhost:8080").path("/weather/"+stationId+"/tempsAll")
                .request().get(String.class);
    	System.out.println("HERE IS "+response.toString()); 
    	
    	double avgFromStation=(double) getAvgFromStation(responseForAll, stationId);
    	double maxFromStation=(double) getMaxFromStation(responseForAll, stationId);
    	double minFromStation=(double) getMinFromStation(responseForAll, stationId);
    	
    	JSONObject json=getJSONObjForTemps(response);
    	double avg= (double) json.get("avg");
    	double min= (double) json.get("min");
    	double max= (double) json.get("max");

        assertEquals(avg, avgFromStation, 0);
        assertEquals(min, minFromStation,0);
        assertEquals(max, maxFromStation, 0);
    }
    
    @Test
    public void testNotFoundStation() throws Exception{
    	
    	Response response= (Response) client.target("http://localhost:8080").path("/weather/20/tempsAll")
    			.request().header("Content-Type","application/json").accept(MediaType.APPLICATION_JSON).get();
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
	  
    private double getMinFromStation(String response, String stationId) throws ParseException {
		
		JSONParser parser = new JSONParser();
    	JSONObject json = (JSONObject) parser.parse("{\"result\":"+response+"}");
    	JSONArray jsonArray = (JSONArray) json.get("result");
    	double minTemp= 1000;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject individualObj = (JSONObject) jsonArray.get(i);
            double current=(double) individualObj.get("temp");
    	    if (current<minTemp) {
    	    	minTemp=current;
    	    }
    	  
        }
		return minTemp;
	}
	private double getMaxFromStation(String response, String stationId) throws ParseException {
		JSONParser parser = new JSONParser();
    	JSONObject json = (JSONObject) parser.parse("{\"result\":"+response+"}");
    	JSONArray jsonArray = (JSONArray) json.get("result");
    	double maxTemp= -1000;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject individualObj = (JSONObject) jsonArray.get(i);
            double current=(double) individualObj.get("temp");
    	    if (current>maxTemp) {
    	    	maxTemp=current;
    	    }
    	  
        }
		return maxTemp;
	}
    private double getAvgFromStation(String response,String stationId) throws ParseException {
   
    	double avgTemp=0;
    	   	
    	JSONParser parser = new JSONParser();
    	JSONObject json = (JSONObject) parser.parse("{\"result\":"+response+"}");
    	JSONArray jsonArray = (JSONArray) json.get("result");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject individualObj = (JSONObject) jsonArray.get(i);
            double current =(double) individualObj.get("temperature");
            avgTemp=avgTemp+current;
    }
        return avgTemp/jsonArray.size();
    }

}
