package edu.asupoly.heal.aqm.dmp;

import java.util.Date;
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
	
	public ServerPushEvent getLastServerPush() throws Exception;
	
	public JSONArray findDylosReadingsForUserBetween(String userId, Date start, Date end) throws Exception;
	
	public boolean addPushEvent(ServerPushEvent s) throws Exception;
}
