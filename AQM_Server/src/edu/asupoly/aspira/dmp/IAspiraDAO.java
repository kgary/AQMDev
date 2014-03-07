package edu.asupoly.aspira.dmp;

import java.util.Date;

import org.json.simple.JSONObject;

import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ServerPushEvent;

/**
 * @author kevinagary
 * This interface defines how we will work with persistent storage
 */
public interface IAspiraDAO {

	AirQualityReadings findAirQualityReadingsForPatientTail(String patientId, int tail) throws Exception;
	AirQualityReadings findAirQualityReadingsForPatient(String patientId) throws Exception;

	boolean importAirQualityReadings(AirQualityReadings toImport, boolean overwrite) throws Exception;
	boolean importAirQualityReadingsJson(JSONObject toImport, boolean overwrite) throws Exception;

	ServerPushEvent getLastServerPush(int type) throws Exception;
	ServerPushEvent getLastServerPush() throws Exception;

	AirQualityReadings findAirQualityReadingsForPatient(String patientId, Date start, Date end) throws Exception;

	boolean addPushEvent(ServerPushEvent s) throws Exception;


	JSONObject getLastServerPushJson(int type) throws Exception;
	JSONObject getLastServerPushJson() throws Exception;
	
	JSONObject findAirQualityReadingsForPatientTailJson(String patientId, int tail) throws Exception;
	JSONObject findAirQualityReadingsForPatientJson(String patientId) throws Exception;

	
	

}
