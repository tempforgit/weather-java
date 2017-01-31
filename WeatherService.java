package io.github.ideaqe.weather;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.ideaqe.weather.provider.*;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.jersey.server.ParamException;
import org.json.simple.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Path("/weather")
public class WeatherService {

    @Created
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void createMeasurement(Observation observation) {
        Observations.getInstance().add(observation);
    }

    @GET
    @Path("/{stationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Observation> getObservations(@PathParam("stationId") int stationId) {
        return Observations.getInstance().getObservations(stationId);
    }

    @GET
    @Path("/{stationId}/{observationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Observation getObservation(@PathParam("stationId") int stationId,
            @PathParam("observationId") int observationId) {
        return Observations.getInstance().getObservation(stationId, observationId);
    }
//Get Temperatures
    @GET
    @Path("/{stationId}/tempsAll")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getAvg(@PathParam("stationId") int stationId) {
    	return Observations.getInstance().getTemps(stationId);
        
    }
//////// with Ranges with three options/////////
    //option 1: queryParam
    @GET
    @Path("/{stationId}/tempRanges")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getTempsFromQueries(@PathParam("stationId") int stationId, @QueryParam("from") String from,
            @QueryParam("to") String to) throws ParseException{
    	String timeRanges=from+":"+to;
     	
    	return Observations.getInstance().iterateObjsForTemps(Observations.getInstance().getNewObs(stationId,timeRanges));       
  }
    //option 2: JSON input
    @GET
    @Path("/{stationId}/temps/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getTempsFromTime(@PathParam("stationId") int stationId, TimeRanges timeobj) throws ParseException{
    	String timeRanges=timeobj.getFrom()+":"+timeobj.getTo();
	
    	return Observations.getInstance().iterateObjsForTemps(Observations.getInstance().getNewObs(stationId,timeRanges));       
  }
    //option 3: string  PathParam with ":" as delimiter
    @GET
    @Path("/{stationId}/temps/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getTime(@PathParam("stationId") int stationId, @PathParam("timestamp") String timestamp) throws ParseException{
    	//Collection<Observation> newobs=Observations.getInstance().getNewObs(stationId,timestamp);  	
    	return Observations.getInstance().iterateObjsForTemps(Observations.getInstance().getNewObs(stationId,timestamp));       
    }
    
    @ThreadSafe
   
    
    private static final class Observations {

        private final Map<Integer, Map<Integer, Observation>> observations = new ConcurrentHashMap<Integer, Map<Integer, Observation>>();
        private static Logger logger = LoggerFactory.getLogger(Observations.class);
        private static final Observations INSTANCE = new Observations();


        private Observations() {
            initialize();
        }

        public static Observations getInstance() {
            return INSTANCE;
        }

        private void initialize() {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            CsvMapper mapper = new CsvMapper();
            ObjectReader reader = mapper.readerFor(Observation.class).with(schema);
            try {
                MappingIterator<Observation> csvData =
                        reader.readValues(Observations.class.getResourceAsStream("/data.csv"));
                csvData.readAll()
                        .stream()
                        .forEach(observation ->
                                observations.computeIfAbsent(observation.stationId, key -> new ConcurrentHashMap<>())
                                        .put(observation.observationId, observation));
            } catch (IOException ex) {
                logger.warn("Could not initialize with prepared CSV file.", ex);
            }
        }

        public Collection<Observation> getObservations(int stationId) {
            ensureExistence(stationId);
            return observations.get(stationId).values();
        }

        public void add(Observation observation) {
            Observation nullIfAssociated = observations
                    .computeIfAbsent(observation.stationId, key -> new ConcurrentHashMap<>())
                    .putIfAbsent(observation.observationId, observation);

            if (nullIfAssociated != null) {
                throw new CollisionException(
                        String.format("Observation for station %s with id %s already exists.",
                                observation.stationId, observation.observationId));
            }
        }

        public Observation getObservation(int stationId, int observationId) {
            ensureExistence(stationId, observationId);
            return observations.get(stationId).get(observationId);
        }

 
        
        private Collection<Observation> getNewObs(int stationId, String date) throws ParseException{
        	ensureExistence(stationId);  
        	ensureDatesFormat(date);   	
        	Collection<Observation> obs=getObservations(stationId);
        	String[] dates=date.split(":");
        	Collection<Observation> newObs=new ArrayList<Observation>();
        	Iterator<Observation> itr = obs.iterator();
            while(itr.hasNext()) {
            	Observation ob=(Observation)itr.next();
            	if (isTimeInRange(dates[0],dates[1], ob)) {
            		newObs.add(ob);
            		
            	}
            }   	
            System.out.println("NEW collection size: "+newObs.size());
        	return newObs;
        }
        
        private String getTime(Observation ob) throws ParseException{
        	
        	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        	String ret="";
            Calendar calendar = Calendar.getInstance();
        	calendar.setTime(ob.getTimestamp());
        	String formatted=format1.format(ob.getTimestamp());
        	ret=formatted;
        
        	return ret;
        }
        private Boolean isTimeInRange(String d1, String d2, Observation ob)throws ParseException {      
        	Boolean inRange=false;
        	//Date timeS=ob.getTimestamp();
        	System.out.println("OB content DEBUGGING"+ob.toString());
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	
            Date date1 = sdf.parse(d1);
            Date date2 = sdf.parse(d2);
            Date fromOb=sdf.parse(getTime(ob));
            
//			TODO: currently, the timestamps for POST (create) from the csv file is GMT timezone, while saving into the observation obj, they
//			got modified to PST timezone
//			need to do something to avoid the confusion while querying the obj in certain range since by 8am of GMT time, it
//			will be automatically transferred the previous day          
            
//            System.out.println("Range From is:"+date1);	
//            System.out.println("Range To is:"+date2);
//            System.out.println("date of Ob in dataset is:"+fromOb);
            if ((fromOb.compareTo(date1)>-1) && (date2.compareTo(fromOb)>-1)) {
            	inRange=true;
            }
           return inRange;
        }
        
        public JSONObject getTemps(int stationId) {
        	ensureExistence(stationId);
            Collection<Observation> obs=getObservations(stationId);
            return iterateObjsForTemps(obs);
        }
        private JSONObject iterateObjsForTemps(Collection<Observation> obs) {
        	JSONObject obj = new JSONObject();
        	Iterator<Observation> itr =  obs.iterator();
        	float totalTemp=0;
        	Boolean start=true;
        	float min=0;
        	float max=0;
            //iterate through the ArrayList values using Iterator's hasNext and next methods
            while(itr.hasNext()) {
            
            	Observation ob=(Observation)itr.next();
            	totalTemp+=ob.getTemp();
            	if (start) {
            		min=ob.getTemp();
            		max=ob.getTemp();
            		start=false;
            	} else {
            		if (ob.getTemp()>max) {
            			max=ob.getTemp();
            		}
            		if (ob.getTemp()<min) {
            			min=ob.getTemp();
            		}
            	}
            	
            }

            obj.put("avg",totalTemp/obs.size());
            obj.put("min",min);
            obj.put("max",max);
            obj.put("Total days", obs.size());
            return obj;
            
        }

        
        private void ensureExistence(int stationId, int observationId) {
            ensureExistence(stationId);
            if (!observations.get(stationId).containsKey(observationId)) {
                throw new NotFoundException();
            }
        }

        private void ensureExistence(int stationId) {
            if (!observations.containsKey(stationId)) {
                throw new NotFoundException();
            }
        }
        
        private boolean isValidDate(String inDate) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            try {
              dateFormat.parse(inDate.trim());
            } catch (ParseException pe) {
              return false;
            }
            return true;
          }
        private void ensureDatesFormat(String date) {
        	String[] dates=date.split(":");
            for (int i=0; i<dates.length; i++) {
            if (!(isValidDate(dates[i]))) {
            	throw new BadRequestException();
            }
            }
        
        }
    }
}
