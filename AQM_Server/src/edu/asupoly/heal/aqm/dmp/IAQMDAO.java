package edu.asupoly.heal.aqm.dmp;

import java.util.Properties;

import org.json.simple.JSONArray;

import edu.asupoly.heal.aqm.model.ServerPushEvent;

/**
 * @author kevinagary This interface defines how we will work with persistent
 *         storage
 */
public interface IAQMDAO {
	public abstract void init(Properties p) throws Exception;

	public boolean importDylosReading(String toImport) throws Exception;
	public boolean importSensordroneReading(String toImport) throws Exception;
	public boolean importReadings(String toImport) throws Exception;

	public JSONArray findDylosReadingsTest() throws Exception;
	public JSONArray findSensordroneReadingsTest() throws Exception;
	public JSONArray findCommonReadingsTest() throws Exception;


	public boolean addPushEvent(ServerPushEvent s) throws Exception;
}
